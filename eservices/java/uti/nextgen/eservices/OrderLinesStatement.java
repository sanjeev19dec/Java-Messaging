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
 * Function to perform "Order Lines Statement" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the api_config.xml file.
 *
 * @author  Gerhard Potgieter
 */

public class OrderLinesStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public OrderLinesStatement()
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
    query.insert(7, "Distinct ");

        if(m_fieldManager.containsTable("REFERENCE"))
		    {
		      query.append(",UTIADM.DOC_LINE_REF_LNK,UTIADM.DOC_LINE_INV_LNK,UTIADM.DOC_ORDER,UTIADM.INVENTORY ");
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
	                query.append(" and "+uField.getFunctionCall()+" = ");
	              }
	              else
	              {
	                query.append(" and "+uField.getFullName()+" = ");
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


	    return query.toString();
	  }
	  //members
	  private String m_where =
		 " 		WHERE UTIADM.DOC_ORDER.DOCUMENT_ID = UTIADM.DOC_LINE.DOCUMENT_ID"+
		" AND utiadm.Doc_Line_Inv_Lnk.Doc_Line_Id = utiadm.Doc_Line.DOC_LINE_ID "+
		" AND    utiadm.Inventory.Inventory_Id   = utiadm.Doc_Line_Inv_Lnk.INVENTORY_ID"+
		" AND    utiadm.Inv_Hist_Lnk.INVENTORY_ID = utiadm.Inventory.Inventory_Id "+
		" AND    utiadm.History.HISTORY_ID     = utiadm.Inv_Hist_Lnk.HISTORY_ID "+
		" AND    utiadm.reference.REFERENCE_ID = utiadm.doc_line_ref_lnk.REFERENCE_ID "+
		" AND    utiadm.doc_line_ref_lnk.DOC_LINE_ID  = utiadm.Doc_Line.DOC_LINE_ID "+
		" AND UTIADM.REFERENCE.REFERENCE_TYPE_CD NOT IN ('PO_NO','MBL_NO','MAWB_NO','LOAD_REF','HB_YEAR','HB_NO')  ";

	 	private String m_nullWhere = " and 1 = 2";

}
