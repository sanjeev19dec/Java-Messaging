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
 * Function to perform "Shipment Detail statement" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservice_config.xml file.
 *
 * @author  Sharon Mothapo
 */

public class ShipmentDetailStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public ShipmentDetailStatement()
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

  if( (m_fieldManager.containsTable("JOURNEY_STOP")) &&
         (m_fieldManager.containsTable("DOCUMENT")))
      {
        query.append(",UTIADM.DOC_JOURNEY_LNK, UTIADM.DOC_ORDER ");
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

    if (hasParameter(parameters,"DOC_ORD-DOCUMENT_ID"))
	   {

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
   }
   	   else
   	   {
   	     //If NodeList is missing a value in key-parameter, then return no data
   	     query.append(m_nullWhere);
	    }

    return query.toString();
  }


  //members
 private String m_where =
" where UTIADM.DOCUMENT.DOCUMENT_TYPE  = 'HB'"+
" and   UTIADM.DOCUMENT.DOCUMENT_ID = UTIADM.DOC_HB.DOCUMENT_ID"+
" and   UTIADM.DOC_JOURNEY_LNK.DOCUMENT_ID(+) = UTIADM.DOCUMENT.DOCUMENT_ID"+
" and   UTIADM.DOC_ORDER.DOCUMENT_ID = UTIADM.DOCUMENT.DOCUMENT_ID"+
" and   UTIADM.DOC_JOURNEY_LNK.DOCUMENT_ID = UTIADM.DOC_ORDER.DOCUMENT_ID"+
" and  (UTIADM.DOC_JOURNEY_LNK.ORIGIN_STOP_ID = UTIADM.JOURNEY_STOP.STOP_ID"+
" or    UTIADM.DOC_JOURNEY_LNK.DESTINATION_STOP_ID = UTIADM.JOURNEY_STOP.STOP_ID) ";

// Include a deliberate false
  private String m_nullWhere = " and 1 = 2 ";



}
