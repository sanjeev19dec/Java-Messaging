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
 * @author  Gerhard Potgieter
 */

public class WaybillDetailStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public WaybillDetailStatement()
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

    if(m_fieldManager.containsTable("document"))
    {
     query.append(",UTIADM.DOC_REF_LNK,UTIADM.DOC_HIST_LNK ");
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


		if(hasParameter(parameters,"REF-REFERENCE_VALUE"))
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
						if(fieldCd != null && fieldCd.equals("REF-REFERENCE_VALUE"))
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
                query.append("Where "+uField.getFunctionCall()+" = ");
              }
              else
              {
                query.append("Where "+uField.getFullName()+" = ");
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
    }else
		{
		 //If NodeList is missing a value in key-parameter, then return no data
		 query.append(m_nullWhere);
		}


    query.append(m_where);

    return query.toString();
  }
  //members
  private String m_where =" AND UTIADM.DOCUMENT.DOCUMENT_ID = UTIADM.DOC_REF_LNK.DOCUMENT_ID"+
		    	" AND UTIADM.DOC_REF_LNK.REFERENCE_ID = UTIADM.REFERENCE.REFERENCE_ID "+
		    	" AND UTIADM.DOC_HIST_LNK.DOCUMENT_ID = UTIADM.DOCUMENT.DOCUMENT_ID "+
		    	" AND UTIADM.HISTORY.HISTORY_ID = UTIADM.DOC_HIST_LNK.HISTORY_ID ";

 	private String m_nullWhere = " where 1 = 2";

}