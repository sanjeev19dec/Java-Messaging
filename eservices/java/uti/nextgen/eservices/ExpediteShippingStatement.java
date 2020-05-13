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
 * Function to perform "Expedite Shipping statement Statement" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the api_config.xml file.
 *
 * @author  Gerhard Potgieter
 */

public class ExpediteShippingStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public ExpediteShippingStatement()
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
    if((m_fieldManager.containsTable("document")) &&
       (m_fieldManager.containsTable("journey_stop")))
    {
      query.append(",UTIADM.DOC_JOURNEY_LNK,UTIADM.DOC_LINE,UTIADM.DOC_INV_LNK ");
    }
  }


  /**
   * Creates the query.
   */

  protected String buildQuery(Document doc)
  {

    StringBuffer query = new StringBuffer();
    StringBuffer parametersQuery = new StringBuffer();
    NodeList parameters = doc.getElementsByTagName("parameter");
    createFrom(query);
    query.append(m_where);
    //query.append(m_where);
		if(hasParameter(parameters,"DOC_LN-DOC_LINE_ID"))
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

			//ELSE BUILD GENERIC SELECT
					else{

						Field uField = getField(fieldCd);

          	if(isFieldAllowed(uField.getFullName()))
          	{
            	if(uField.hasValidFunctionCall())
            	{
             	parametersQuery.append(" and "+uField.getFunctionCall()+" LIKE ");
            	}
            	else
            	{
             	parametersQuery.append(" and "+uField.getFullName()+" LIKE ");
            	}
            	if(uField.getType().equals("NUMBER"))
            	{
             	parametersQuery.append("'"+value+"%' ");
            	}
            	else
            	{
             	parametersQuery.append("'"+value+"%' ");
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
     //If parameters NodeList contain no search values, then return no data
    	if (parametersQuery.length() == 0)
		  {
		   query.append(m_nullWhere);
		  }
		  else {
		   query.append(parametersQuery);

		  }
      return query.toString();
  		}

  //members
  private String m_where = " WHERE UTIADM.DOCUMENT.DOCUMENT_ID = UTIADM.DOC_LINE.DOCUMENT_ID "+
	   												" AND UTIADM.DOC_JOURNEY_LNK.DOCUMENT_ID(+) = UTIADM.DOCUMENT.DOCUMENT_ID "+
	   												" AND (UTIADM.DOC_JOURNEY_LNK.ORIGIN_STOP_ID = UTIADM.JOURNEY_STOP.STOP_ID "+
	   												" OR  UTIADM.DOC_JOURNEY_LNK.DESTINATION_STOP_ID = UTIADM.JOURNEY_STOP.STOP_ID)"+
								   					" AND DOC_HB.DOCUMENT_ID(+) = UTIADM.DOCUMENT.DOCUMENT_ID"+
	   												" AND UTIADM.DOC_INV_LNK.DOCUMENT_ID = UTIADM.DOCUMENT.DOCUMENT_ID"+
	   												" AND UTIADM.INVENTORY.INVENTORY_ID = UTIADM.DOC_INV_LNK.INVENTORY_ID ";
  // Include a deliberate false
	private String m_nullWhere = " and 1 = 2";

}
