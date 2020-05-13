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
 * Function to perform "Order statement" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author  Sanjeev Sharma
 */

public class OrderStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public OrderStatement()
  {
  }


  /**
   * Creates the select-from part of the query.
   *
   * @param  query  Query buffer.
   */

  private void createFrom(StringBuffer query)
  {
    createInitialFrom(query);

    if((m_fieldManager.containsTable("location")) &&
       (m_fieldManager.containsTable("document")))
    {
      query.append(",UTIADM.Loc_Doc_Lnk ");
    }
  }


  /**
   * Creates the query.
   */

  protected String buildQuery(Document doc)
  {
    StringBuffer query = new StringBuffer();
    NodeList parameters = doc.getElementsByTagName("parameter");

    createFrom(query);
    query.append(m_where);

    for(int i = 0; i < parameters.getLength(); i++)
    {
      try
      {
        Node idNode = m_dom.getElement("field_cd",(Element)parameters.item(i));
        Node valNode = m_dom.getElement("value",(Element)parameters.item(i));

        String fieldId = m_dom.getTextNodeValue(idNode);
        String value = m_dom.getTextNodeValue(valNode);

        if(!(isValuePairValid(fieldId,value)))
        {
          continue;
        }
        else
        {
          Field uField = getField(fieldId);

          if(isFieldAllowed(uField.getFullName()))
          {
            if(uField.hasValidFunctionCall())
            {
              query.append("and "+uField.getFunctionCall()+" = ");
            }
            else
            {
              query.append("and "+uField.getFullName()+" = ");
            }

            if(uField.getType().equals("NUMBER"))
            {
              query.append(value+" ");
            }
            else
            {
              query.append("'"+value+"' ");
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

    return query.toString();
  }


  //members
  private String m_where =
  "where  UTIADM.DOCUMENT.DOCUMENT_ID In (Select  DP.Document_ID "+
  "                            from   UTIADM.Doc_Par_Lnk DP, "+
  "                                   UTIADM.Participant     PC "+
  "                            where  PC.UTRAC_CD = '9999999999' "+
  "                            and    DP.PARTICIPANT_ID  = PC.PARTICIPANT_ID) "+
  "and UTIADM.DOCUMENT.DOCUMENT_TYPE      = 'PO' "+
  "and UTIADM.DOCUMENT.STATUS_CD          = 'RG' "+
  "and UTIADM.LOC_DOC_LNK.DOC_LOC_TYPE_CD = 'DWH' "+
  "and UTIADM.LOC_DOC_LNK.DOCUMENT_ID     = UTIADM.DOCUMENT.DOCUMENT_ID "+
  "and UTIADM.LOCATION.LOCATION_TYPE      = 'WAREHOUSE' "+
  "and UTIADM.LOC_DOC_LNK.LOCATION_ID     = UTIADM.LOCATION.LOCATION_ID";
}
