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
 * Function to perform "Purchase Order Line Header" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author  Wendy Tessendorf
 */

public class POLineHeader extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public POLineHeader()
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

    if((m_fieldManager.containsTable("inventory")) &&
       (m_fieldManager.containsTable("doc_line")))
    {
      query.append(",UTIADM.DOC_LINE_INV_LNK ");
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

    if (hasParameter(parameters,"DOC_LN-DOC_LINE_ID"))
    {
      for(int i = 0; i < parameters.getLength(); i++)
      {
        try
        {
          Node idNode = m_dom.getElement("field_cd",(Element)parameters.item(i));
          Node valNode = m_dom.getElement("value",(Element)parameters.item(i));

          String fieldCd = m_dom.getTextNodeValue(idNode);
          String value = m_dom.getTextNodeValue(valNode);

          if(!(isValuePairValid(fieldCd,value)))
          {
            if(fieldCd != null && fieldCd.equals("DOC_LN-DOC_LINE_ID"))
            {
              query.append(m_nullWhere);
              return query.toString();
            }
            continue;
          }
          else
          {
            Field field = getField(fieldCd);

            if(isFieldAllowed(field.getFullName()))
            {
              if(field.hasValidFunctionCall())
              {
                query.append(" and "+field.getFunctionCall()+" = ");
              }
              else
              {
                query.append(" and "+field.getFullName()+" = ");
              }

              if(field.getType().equals("NUMBER"))
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
    } //hasParameter()
    else
    {
     //If NodeList is missing a value in key-parameter, then return no data
      query.append(m_nullWhere);
    }

      return query.toString();
  }


  //members
  private String m_where =  " where  UTIADM.DOC_LINE.DOC_LINE_TYPE = 'PO_LINE'"+
  " and UTIADM.DOC_LINE.DOCUMENT_ID = UTIADM.DOCUMENT.DOCUMENT_ID "+
  " and UTIADM.DOC_LINE.DOC_LINE_ID = UTIADM.DOC_LINE_ORDER.DOC_LINE_ID ";
/*and UTIADM.DOC_LINE.DOC_LINE_ID = UTIADM.DOC_LINE_INV_LNK.DOC_LINE_ID
AND UTIADM.INVENTORY.INVENTORY_ID = UTIADM.DOC_LINE_INV_LNK.INVENTORY_ID*/

  // Include a deliberate false
  private String m_nullWhere = " and 1 = 2";

}
