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
 * Function to perform "Output Report statement" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservice_config.xml file.
 *
 * @author  Sharon Mothapo
 */

public class OutputReportStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public OutputReportStatement()
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
          Field field = getField(fieldId);

          if(isFieldAllowed(field.getFullName()))
          {
            if(field.hasValidFunctionCall())
            {
              query.append("and "+field.getFunctionCall()+" = ");
            }
            else
            {
              query.append("and "+field.getFullName()+" = ");
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

    return query.toString();
  }


  //members
 private String m_where =
  " where 1 = 1 ";

}
