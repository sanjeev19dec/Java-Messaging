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
 * Function to perform "Shipment History statement" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author  Wendy Tessendorf
 */

public class ShipmentHistoryStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public ShipmentHistoryStatement()
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

    query.append(",UTIADM.DOC_HB");

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
  private String m_where =  " Where BWADM.V_HB_HISTORY.DOCUMENT_ID = UTIADM.DOC_HB.DOCUMENT_ID ";

  // Include a deliberate false
  private String m_nullWhere = " and 1 = 2";

}
