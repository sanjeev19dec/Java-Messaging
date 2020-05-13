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

public class OnOrder_Hitlist extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public OnOrder_Hitlist()
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

    if(!m_fieldManager.containsTable("DOC_LINE"))
    {
      query.append(",UTIADM.DOC_LINE ");
    }
    if(!m_fieldManager.containsTable("REFERENCE"))
    {
      query.append(",UTIADM.REFERENCE ");
    }
    if(!m_fieldManager.containsTable("DOC_REF_LNK"))
    {
      query.append(",UTIADM.DOC_REF_LNK ");
    }
    if(!m_fieldManager.containsTable("DOC_PAR_LNK"))
    {
      query.append(",UTIADM.DOC_PAR_LNK ");
    }
    if(!m_fieldManager.containsTable("DOCUMENT"))
    {
      query.append(",UTIADM.DOCUMENT ");
    }
    if(!m_fieldManager.containsTable("DOC_ORDER"))
    {
      query.append(",UTIADM.DOC_ORDER ");
    }
  }


  /**
   * Creates the query.
   */

  protected String buildQuery(Document doc)
  {
    String functionId = m_dom.getTextNodeValue("function_id",doc);
    StringBuffer query = new StringBuffer();
    StringBuffer minusInventory = new StringBuffer();
    NodeList parameters = doc.getElementsByTagName("parameter");
    String basicWhere_params = "";
    int replaceInd = 0;

    createFrom(query);

    query.insert(7, "Distinct ");

    if (hasParameter(parameters,"REF--REFERENCE_VALUE"))
    {
      if (hasParameter(parameters,"ON_ORDER_LATE"))
      {
	    query.append(basicWhere);
	    query.append(basicWhere_late);
      }
	  else
      {
	    query.append(basicWhere);
	    query.append(basicWhere_on_time);

      }
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
				basicWhere_params = basicWhere_params + "and " + uField.getFunctionCall() + " = ";
              }
              else
              {
                basicWhere_params = basicWhere_params + "and " + uField.getFullName() + " = ";
              }

              if(uField.getType().equals("NUMBER"))
              {
                basicWhere_params = basicWhere_params + value + " ";
              }
              else
              {
				basicWhere_params = basicWhere_params + "'" + value + "' ";
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

      /*  Manipulate a DECODE statement inside the DB_FUNCTION_CALL: (p_metric) ||
      ||  If 'LATE':          sum(DOC_LINE.qty)                                 ||
      ||  If 'ON_TIME':       sum(DOC_LINE.qty) - sum(Inventory.Qty_Shipped)    */

      replaceInd = query.indexOf("p_metric");
      if (replaceInd != 0) {
        if (hasParameter(parameters,"ON_ORDER_LATE"))  {

             query.replace(replaceInd, replaceInd+8, "'LATE'");
        }
         else  {
             query.replace(replaceInd, replaceInd+8, "'ON_TIME'");
        }
      }


    }
    else
    {
     //If NodeList is missing a value in key-parameter, then return no data
      query.append(m_nullWhere);
    }

    query.append(basicWhere_params);
    return query.toString();
  }


  //members
  //Total orders
  private String basicWhere =
  " where utiadm.document.document_type = 'PO' " +
     " AND utiadm.document.status_cd = 'RG' " +
     " AND utiadm.doc_order.order_type = 'PUR' " +
     " AND utiadm.doc_order.document_id = utiadm.doc_line.document_id " +
     " AND utiadm.doc_par_lnk.document_id = utiadm.document.document_id " +
     " AND utiadm.reference.reference_id = utiadm.doc_ref_lnk.reference_id " +
     " and utiadm.doc_ref_lnk.document_id = utiadm.document.document_id "+
     " and utiadm.document.document_id = utiadm.doc_order.document_id ";


  //On time orders
  private String basicWhere_on_time = "AND (SYSDATE <= NVL(utiadm.doc_order.latest_ship_date,SYSDATE) " +
                                   "AND SYSDATE <= NVL(NVL(utiadm.Query_Pkg.Get_Doc_Line_Event_Plan_Date(utiadm.doc_line.doc_line_id,'ROS'), utiadm.Query_Pkg.Get_Doc_Event_Plan_Date(utiadm.document.document_id,'ROS')),SYSDATE) " +
                                   "AND SYSDATE <= NVL(NVL(utiadm.Query_Pkg.get_Doc_Line_Event_Plan_Date(utiadm.doc_line.doc_line_id,'EXPECT_READY'), utiadm.Query_Pkg.Get_Doc_Event_Plan_Date(utiadm.document.document_id,'EXPECT READY')), SYSDATE)) ";

  //Late orders
  private String basicWhere_late = "AND (SYSDATE > utiadm.doc_order.latest_ship_date " +
                                "OR SYSDATE > NVL(utiadm.Query_Pkg.Get_Doc_Line_Event_Plan_Date(utiadm.doc_line.doc_line_id,'ROS'), utiadm.Query_Pkg.Get_Doc_Event_Plan_Date(utiadm.document.document_id,'ROS')) " +
                                "OR SYSDATE > NVL(utiadm.Query_Pkg.get_Doc_Line_Event_Plan_Date(utiadm.doc_line.doc_line_id,'EXPECT_READY'), utiadm.Query_Pkg.Get_Doc_Event_Plan_Date(utiadm.document.document_id,'EXPECT READY'))) ";

  // Include a deliberate false
  private String m_nullWhere = " where 1 = 2 ";
}
