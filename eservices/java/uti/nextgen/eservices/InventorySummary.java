/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.eservices;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.w3c.dom.*;
import uti.nextgen.tools.*;
import uti.nextgen.eservices.util.*;

/**
 * Function to perform "Inventory" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author  Sanjeev Sharma
 */

public class InventorySummary extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public InventorySummary()
  {
  }


  /**
   * Creates and returns the String of reference types and values.
   *
   * @param  parameters  NodeList of parameter elements.
   *
   * @return  String containing the reference type/value String.
   */

  private String getReferenceString(NodeList parameters)
  {
    LinkedList types = new LinkedList();
    LinkedList values = new LinkedList();

    Field typeField = null;
    Field valueField = null;

    try
    {
      typeField = getField("77");
      valueField = getField("78");
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }

    for(int i = 0; i < parameters.getLength(); i++)
    {
      Node idNode = m_dom.getElement("field_cd",(Element)parameters.item(i));
      Node valNode = m_dom.getElement("value",(Element)parameters.item(i));

      String fieldId = m_dom.getTextNodeValue(idNode);
      String value = m_dom.getTextNodeValue(valNode);

      if(isValuePairValid(fieldId,value))
      {
        if(fieldId.equals("77"))
        {
          types.add(value);
        }
        else if(fieldId.equals("78"))
        {
          values.add(value);
        }
      }
    }

    if(types.size() > 0)
    {
      int count = 0;
      StringBuffer buffer = new StringBuffer();

      buffer.append("where (VI."+typeField.getName()+",");
      buffer.append("VI."+valueField.getName()+") In (");

      ListIterator typeIter = types.listIterator();
      ListIterator valuesIter = values.listIterator();

      while((typeIter.hasNext()) && (valuesIter.hasNext()))
      {
        if(count == 0)
        {
          buffer.append("('"+(String)typeIter.next()+"', '"+
                        (String)valuesIter.next()+"')");
          count++;
        }
        else
        {
          buffer.append(",('"+(String)typeIter.next()+"', '"+
                        (String)valuesIter.next()+"')");
        }
      }

      buffer.append(") ");

      return buffer.toString();
    }
    else
    {
      return null;
    }
  }


  /**
   * Creates the query.
   */

  protected String buildQuery(Document doc)
  {
    boolean appendedField = false;
    String finalLoc = null;
    StringBuffer locBuffer = null;
    StringBuffer buffer = new StringBuffer();

    buffer.append(m_top);

    NodeList parameters = doc.getElementsByTagName("parameter");

    for(int i = 0; i < parameters.getLength(); i++)
    {
      try
      {
        Node idNode = m_dom.getElement("field_id",(Element)parameters.item(i));
        Node valNode = m_dom.getElement("value",(Element)parameters.item(i));

        String fieldId = m_dom.getTextNodeValue(idNode);
        String value = m_dom.getTextNodeValue(valNode);

        if(isValuePairValid(fieldId,value))
        {
          if((!(fieldId.equals("77"))) &&
             (!(fieldId.equals("78"))))
          {
            if(fieldId.equals("111"))
            {
              if(!(appendedField))
              {
                appendedField = true;
                locBuffer = new StringBuffer();
                Field field = getField(fieldId);

                locBuffer.append("and L."+field.getColumn()+" ");
                locBuffer.append("In ('"+value+"'");

                finalLoc = new String("and VI.Location_utrac_cd = '"+
                                      value+"' ");
              }
              else
              {
                locBuffer.append(",'"+value+"'");
              }
            }
            else
            {
              Field field = getField(fieldId);

              if(isFieldAllowed(field.getFullName()))
              {
                if(field.hasValidFunctionCall())
                {
                  buffer.append("and "+field.getFunctionCall()+" = ");
                }
                else
                {
                  buffer.append("and "+field.getFullName()+" = ");
                }

                if(field.getType().equals("NUMBER"))
                {
                  buffer.append(value+" ");
                }
                else
                {
                  buffer.append("'"+value+"' ");
                }
              }
            }
          }
        }
      }
      catch (NullPointerException npe)
      {
        npe.printStackTrace();
      }
      catch (SQLException sqle)
      {
        sqle.printStackTrace();
      }
    }

    if(locBuffer != null)
    {
      locBuffer.append(") ");
      buffer.append(locBuffer.toString());
    }

    String reference = getReferenceString(parameters);

    if(reference != null)
    {
      buffer.append(m_firstUnion+reference);
    }
    else
    {
      buffer.append(m_firstUnion);
    }

    if(locBuffer != null)
    {
      buffer.append(finalLoc);
    }

    buffer.append(m_bottom);
    return buffer.toString();
  }


  //members
  private String m_top =
  "Select T.Heading, T.Total "+
  "from (Select 'On Order' heading, nvl(Sum(DLO.QTY_ORDERED),0) total, 1 ord "+
  "From   UTIADM.Document D, "+
  "     UTIADM.Loc_Doc_Lnk LD, "+
  "     UTIADM.Doc_Line    DL, "+
  "     UTIADM.Doc_Line_Order DLO, "+
  "     UTIADM.Location    L "+
  "Where  D.Document_ID     In (Select  DP.Document_ID "+
  "                            From   UTIADM.Doc_Par_Lnk DP, "+
  "                                   UTIADM.Participant PC "+
  "                            Where  PC.UTRAC_CD = '9999999999' "+
  "                            And    DP.PARTICIPANT_ID  = PC.PARTICIPANT_ID) "+
  "And    D.DOCUMENT_TYPE    = 'PO' "+
  "And    D.STATUS_CD        = 'RG' "+
  "And    LD.DOC_LOC_TYPE_CD = 'DWH' "+
  "And    LD.DOCUMENT_ID     = D.DOCUMENT_ID "+
  "And    DL.DOCUMENT_ID     = D.DOCUMENT_ID "+
  "And    DL.DOC_LINE_TYPE   = 'PO_LINE' "+
  "And    DLO.DOC_LINE_ID    = DL.DOC_LINE_ID "+
  "And    L.LOCATION_TYPE    = 'WAREHOUSE' "+
  "And    LD.LOCATION_ID     = L.LOCATION_ID ";

  private String m_firstUnion =
  "UNION "+
  "Select 'In Transit' heading, nvl(Sum(Inv.Qty),0) total, 2 ord "+
  "From   UTIADM.Inventory Inv, "+
  "       (Select I.INVENTORY_ID "+
  "        From   UTIADM.Inv_Doc_Lnk I "+
  "        Where  I.DOCUMENT_ID In (Select D.document_ID "+
  "                                 From   (Select DP.DOCUMENT_ID "+
  "                                         From   UTIADM.Doc_Par_Lnk DP, "+
  "                                                UTIADM.Participant PC "+
  "                                         Where  PC.UTRAC_CD = '9999999999' "+
  "                        And    DP.PARTICIPANT_ID  = PC.PARTICIPANT_ID) P, "+
  "                                        UTIADM.Document    D "+
  "                                 Where  D.DOCUMENT_ID   = P.Document_ID "+
  "                                 And    D.DOCUMENT_TYPE = 'HB' "+
  "                                 And    D.STATUS_CD     <> 'DC')) DI "+
  "Where  Inv.Inventory_Id = DI.Inventory_ID "+
  "UNION "+
  "Select 'In Warehouse' heading, nvl(Sum(VI.QTY),0) total, 3 ord "+
  "From   UTIADM.V_Inventory VI ";

  private String m_bottom = " ORDER BY 3) t";
}
