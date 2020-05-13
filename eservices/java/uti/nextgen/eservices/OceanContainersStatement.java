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
 * Function to perform "PO Buyer Details Header" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author  Gerhard Potgieter
 */

public class OceanContainersStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public OceanContainersStatement()
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
    if (!m_fieldManager.containsTable("doc_hb"))
    {
      query.append(",UTIADM.DOC_HB,UTIADM.DOC_INV_LNK,UTIADM.DOCUMENT  ");
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
    if (hasParameter(parameters,"DOC_HB-DOCUMENT_ID"))
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
						if(fieldCd != null && fieldCd.equals("DOC_HB-DOCUMENT_ID"))
						{
							query.append(m_nullWhere);
							return query.toString();
						}
            continue;
          }
          else
          {
            Field uField = getField(fieldCd);

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
    }
    else
    {
     //If NodeList is missing a value in key-parameter, then return no data
      query.append(m_nullWhere);
    }

    return query.toString();
  }


  //members
  private String m_where = " where UTIADM.DOCUMENT.DOCUMENT_ID = UTIADM.DOC_HB.DOCUMENT_ID "+
  													" AND UTIADM.DOC_INV_LNK.DOCUMENT_ID = UTIADM.DOCUMENT.DOCUMENT_ID"+
  													" AND UTIADM.INV_CONTAINER.INVENTORY_ID = UTIADM.DOC_INV_LNK.INVENTORY_ID"+
  													" AND UTIADM.ADAP_CONTAINER.CONTAINER_ID = UTIADM.INV_CONTAINER.CONTAINER_ID " ;


  // Include a deliberate false
  private String m_nullWhere = " and 1 = 2";

}
