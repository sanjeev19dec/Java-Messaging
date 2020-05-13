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
 * Function to perform "Order Line Statement" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the api_config.xml file.
 *
 * @author  Gerhard Potgieter
 */

public class PurchaseOrderStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public PurchaseOrderStatement()
  {
  }

  /** Build up the Where to search by a Date Range,
   */
  private StringBuffer buildDateRange(String searchDateType,
                                      String startDate,
                                      String endDate)
  {
    StringBuffer dateString = new StringBuffer();

    if ((startDate != null) && (endDate != null) && (searchDateType != null)) {
      dateString.append(" and "+searchDateType+" >= '"+startDate+"'");
      dateString.append(" and "+searchDateType+" <= '"+endDate+"'");
    }
    return dateString;
  }

  /** The function will return true if the String
   *  passed through is in a Valid Date Format
   */
  boolean validDate(String date) {
    if (date.length() < 7) return false;
    String days = date.substring(0,2);
    String months = date.substring(2,5);
    String years = date.substring(5,7);
    Integer DD = null;
    String mm = months;
    Integer YY = null;
    int dd = 0;
    int yy = 0;
    try {
      DD = new Integer(days);
      YY = new Integer(years);
    }
    catch (NumberFormatException e) {
      return false;
    }
    dd = DD.intValue();
    yy = YY.intValue();
    if (dd < 1) {
      return false;
    }
    if (yy < 0) {
      return false;
    }
    if ((!mm.equals("JAN"))&&(!mm.equals("FEB"))&&(!mm.equals("MAR"))&&(!mm.equals("APR"))&&(!mm.equals("MAY"))&&(!mm.equals("JUN"))&&(!mm.equals("JUL"))&&(!mm.equals("AUG"))&&(!mm.equals("SEP"))&&(!mm.equals("OCT"))&&(!mm.equals("NOV"))&&(!mm.equals("DEC"))) return false;
    if ((mm.equals("JAN"))||(mm.equals("MAR"))||(mm.equals("MAY"))||(mm.equals("JUL"))||(mm.equals("AUG"))||(mm.equals("OCT"))||(mm.equals("DEC"))) {
        if (dd > 31) return false;
    }
    if ((mm.equals("APR"))||(mm.equals("JUN"))||(mm.equals("SEP"))||(mm.equals("NOV"))){
      if (dd > 30) return false;
    }
    if (mm.equals("FEB")) {
      if ((yy % 4) != 0){
        if (dd > 28) return false;
      } else {
        if (dd > 29) return false;
      }
    }
  return true;
  }

  /**
   * Creates the select-from part of the query.
   *
   * @param  query  Query buffer.
   */

  private void createFrom(StringBuffer query)
  {
    createInitialFrom(query);
        if(!(m_fieldManager.containsTable("REFERENCE")))
		    {
		      query.append(",UTIADM.DOC_REF_LNK,UTIADM.REFERENCE ");
    		}
    		else
    		{
				 query.append(",UTIADM.DOC_REF_LNK");
				}
  }


  /**
   * Creates the query.
   */

  protected String buildQuery(Document doc)
  {

    StringBuffer query = new StringBuffer();
    StringBuffer parametersQuery = new StringBuffer();
    String functionId = m_dom.getTextNodeValue("function_id",doc);
    String searchDateType = null;
		String startDate = null;
    String endDate = null;
		boolean searchingByRef = false;

    NodeList parameters = doc.getElementsByTagName("parameter");

    createFrom(query);
		query.append(m_where);

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

				else{

          Field uField = getField(fieldCd);
/* This parameter shows which DATE to search by:
 *        If BO - booking date (default),
 *           RV - freight received date
 *           STA - status date
 */
							 if(fieldCd.equals("SELECT-SEARCHDATE")) {
                if (value.equals("ORD")) {
	                  searchDateType = "UTIADM.DOC_ORDER.RAISED_DATE";
                }
                else if (value.equals("STA")) {
	                  searchDateType = "UTIADM.DOCUMENT.STATUS_DATE";
                }
                else if (value.equals("ERD")) {
										searchDateType = "TO_CHAR(UTIADM.Query_Pkg.Get_Doc_Event_Plan_Date(UTIADM.DOCUMENT.DOCUMENT_ID, 'EXPECT_READY'),'DDMONYY')";
                }
                else if (value.equals("ROD")) {
										searchDateType = "TO_CHAR(UTIADM.Query_Pkg.Get_Doc_Event_Plan_Date(UTIADM.DOCUMENT.DOCUMENT_ID, 'ROS'),'DDMONYY')";
                }
	            }
/*  Date Range to search by: Start & End Dates
 *  used inside buidDateRange() method
 */
							else if(fieldCd.equals("SELECT-STARTDATE")) {

							  if (validDate(value)) {
							      startDate = value;
							 }
							}
							else if(fieldCd.equals("SELECT-ENDDATE")) {

							  if (validDate(value)) {
							      endDate = value;
							  }
            	}
            	else if(fieldCd.equals("REF-REFERENCE_VALUE"))
								{
					  		 searchingByRef = true;

      					}

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

      } //try
      catch (NullPointerException npe)
      {
        npe.printStackTrace();
      }
      catch (SQLException sqle)
      {
        sqle.printStackTrace();
      }




    }
    parametersQuery.append(buildDateRange(searchDateType,startDate,endDate));

    //If parameters NodeList contain no search values, then return no data
    if (parametersQuery.length() == 0)
		    {
		      query.append(m_nullWhere);
		    }
		    else {
		      query.append(parametersQuery);

		    }

				if(functionId.equals("5")&&(!searchingByRef)){
						query.append(m_nullWhere);
				}

		    return query.toString();
  			}



  //members
   private String m_where = " where UTIADM.DOCUMENT.DOCUMENT_ID = UTIADM.DOC_ORDER.DOCUMENT_ID "+
            						    " AND UTIADM.DOC_REF_LNK.REFERENCE_ID = UTIADM.REFERENCE.REFERENCE_ID"+
            						    " AND UTIADM.DOCUMENT.DOCUMENT_ID = UTIADM.DOC_REF_LNK.DOCUMENT_ID "+
                            " AND UTIADM.DOCUMENT.DOCUMENT_TYPE = 'PO' ";

	private String m_nullWhere = " and 1 = 2";
}
