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

public class PurchaseOrderHeader extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public PurchaseOrderHeader()
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
      query.append(",UTIADM.DOC_LINE ");
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


		if(hasParameter(parameters,"DOC_ORD-DOCUMENT_ID"))
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
						if(fieldCd != null && fieldCd.equals("DOC_ORD-DOCUMENT_ID"))
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
  private String m_where =
		    	" AND UTIADM.DOC_ORDER.DOCUMENT_ID(+) = UTIADM.DOCUMENT.DOCUMENT_ID"+
		    	"	AND UTIADM.DOC_INSTR_LINE.DOCUMENT_ID(+) = UTIADM.DOCUMENT.DOCUMENT_ID"+
	      	" AND UTIADM.DOC_LINE_ORDER.DOC_LINE_ID = UTIADM.DOC_LINE.DOC_LINE_ID"+
	  	   	" GROUP BY UTIADM.DOCUMENT.DOCUMENT_ID,UTIADM.DOC_ORDER.ORDER_TYPE,UTIADM.DOC_ORDER.RAISED_DATE,UTIADM.DOC_ORDER.TIMESTAMP,"+
	  	   	" UTIADM.DOCUMENT.STATUS_CD,UTIADM.DOCUMENT.STATUS_DATE,UTIADM.DOC_ORDER.EXPEDITING_PRIORITY,"+
	  	   	" UTIADM.DOC_ORDER.TOT_VALUE_OF_ORDER,UTIADM.DOC_ORDER.TERMS_OF_SALE,UTIADM.DOC_ORDER.REVISION_NO,"+
	  	   	" UTIADM.DOC_ORDER.EARLIEST_SHIP_DATE, UTIADM.DOC_ORDER.LATEST_SHIP_DATE,UTIADM.DOC_ORDER.ORDER_MODE,UTIADM.DOC_INSTR_LINE.INSTR_LINE_TEXT";

 	private String m_nullWhere = " where 1 = 2";
}

