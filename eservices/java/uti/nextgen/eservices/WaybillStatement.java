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
 * Function to perform "Waybill statement" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author  Wendy Tessendorf
 */

public class WaybillStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public WaybillStatement()
  {
  }



  /** Builds status column name, relational operator (In / Not In)
   *  and list of status search values into one String
   */
  private StringBuffer buildStatusString(StringBuffer statusBuffer,
                                 StringBuffer statusListBuffer,
                                 StringBuffer statusIncludeBuffer)
  {
    if ((statusBuffer.length() != 0) && (statusListBuffer.length() != 0))
    {
      statusBuffer.append(statusIncludeBuffer);
      statusBuffer.append(statusListBuffer);
      statusBuffer.append(") ");
    }
    return statusBuffer;
  }




  /** Builds Customer Ref column, any relational operator
   *  (= or Like) and search value into a String
   */
  private StringBuffer buildCustRefString( StringBuffer custRefBuffer,
                                           StringBuffer custRefValue,
                                           int match)
  {
    if ((custRefBuffer.length() != 0) && (custRefValue.length() != 0) )
    {
      if (match == 0) {

         custRefBuffer.append(" = ");
         custRefBuffer.append(custRefValue);
      }
      else //Let 'STARTS WITH' be default (match == 1)
      {
         custRefBuffer.append(" LIKE ");
         custRefBuffer.append(custRefValue+ "||'%'");
      }
    }
    return custRefBuffer;
  }



  /** If the user is searching by Client Reference Value,
   *  add all Reference Tables and table Joins to the query
   */
  private void addRefClauses(StringBuffer query,
                             StringBuffer parametersQuery)
  {
    query.insert(7, "Distinct ");

    int fromIndex = query.indexOf("from ");
    query.insert(fromIndex+5, ReferenceFrom);

    if(!(m_fieldManager.containsTable("REFERENCE")) )
    {
      query.insert(fromIndex+5, ReferenceTable);
    }

    parametersQuery.append(ReferenceJoin);
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

    if((m_fieldManager.containsTable("DOC_HB")) &&
       (m_fieldManager.containsTable("DOC_MB")))
    {
      query.append(",UTIADM.DOC_DOC_LNK");
    }

  }


  /**
   * Creates the query.
   */

  protected String buildQuery(Document doc)
  {
    StringBuffer query = new StringBuffer();
    StringBuffer parametersQuery = new StringBuffer();
    StringBuffer statusBuffer = new StringBuffer();
    StringBuffer statusListBuffer = new StringBuffer();
    StringBuffer custRefBuffer = new StringBuffer();
    StringBuffer custRefValue = new StringBuffer();
    String operator = null;
    String searchDateType = null;
    String startDate = null;
    String endDate = null;

    StringBuffer statusIncludeBuffer = new StringBuffer("In (");

    int match = 1;
    int typeValueInd = 0;
    int countryValueInd = 0;
    int locationTypeInd = 0;
    boolean searchingByRef = false;


    String functionId = m_dom.getTextNodeValue("function_id",doc);

    NodeList parameters = doc.getElementsByTagName("parameter");

    createFrom(query);

    query.append(DocWhere);

    // Append more Link Tables to the FROM
    if( m_fieldManager.containsTable("DOC_HB"))
    {
      query.append(DocHbJoin);
    }

    if( m_fieldManager.containsTable("DOC_MB"))
    {
      query.append(DocMBJoin);
    }


    // Loop through the parameters
    for(int i = 0; i < parameters.getLength(); i++)
    {
      try
      {
        Node cdNode = m_dom.getElement("field_cd",(Element)parameters.item(i));
        Node valNode = m_dom.getElement("value",(Element)parameters.item(i));

        String fieldCd = m_dom.getTextNodeValue(cdNode);
        String value = m_dom.getTextNodeValue(valNode);

        // If no value inside the parameter, loop back up
        if(!(isValuePairValid(fieldCd,value)))
        {
          continue;
        }
        else
        {
          Field uField = getField(fieldCd);

          //Work out which relational operator each column needs
          if (fieldCd.equals("DOC_HB-SHIPPER_NAME")   ||
              fieldCd.equals("DOC_HB-CONSIGNEE_NAME") ||
              fieldCd.equals("DOC_HB-HB_NO")          ){
          operator = " like ";
          }
          else {
          operator = " = ";
          }

          /*  Get Status column_name, Status list and relational
           *  operator, then append altogether inside buildStatusString()
           */
          if(fieldCd.equals("DOC-STATUS_CD") && isFieldAllowed(uField.getFullName()) )
          {
              String statusList = null;
              Field field = getField(fieldCd);

              statusBuffer.append("and "+field.getFullName()+" ");

              statusList = value.replaceAll(" ","");
              statusList = statusList.replaceAll(",","','");

              statusListBuffer.append("'"+statusList+"'");

          }
          /* Gather Customer_Ref column_name, Cust Ref search value and relational
           * operator, then append altogether in buildCustRefString()
           */
          else if(fieldCd.equals("REF-REFERENCE_VALUE") && isFieldAllowed(uField.getFullName()))
          {
            searchingByRef = true;

              Field field = getField(fieldCd);

              custRefBuffer.append("and "+field.getFullName());
              custRefValue.append("'"+value+"'");

          }
          /* For Waybill:  This parameter means 'Include' or 'Exclude' HB Statuses
           * For Cust Ref: This parameter means 'Starts With' or 'Exact Match' the Reference Value
           */
          else if(fieldCd.equals("RADIO-OPTIONS")) {

              if (value.equals("EXC") && (statusIncludeBuffer.length() < 5)) {

                statusIncludeBuffer.insert(0,"Not ");
              }
              else if (value.equals("EXA")) {
                match = 0;
              }
              else if (value.equals("STA")) {
                match = 1;
              }
          }

          /* This parameter shows which DATE to search by:
           *        If BO - booking date (default),
           *           RV - freight received date
           *           STA - status date
           */
          else if(fieldCd.equals("SELECT-SEARCHDATE")) {

              if (value.equals("RV")) {

                searchDateType = "UTIADM.DOC_HB.FREIGHT_RECEIVED_DATE";
              }
              else if (value.equals("STA")) {

                searchDateType = "UTIADM.DOCUMENT.STATUS_DATE";
              }
              else {
                searchDateType = "UTIADM.DOC_HB.BOOKING_DATE";
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

          /*  Build a special subquery to find the ORIGIN Country
           */
          else if(fieldCd.equals("DOC_HB-ORIGIN_COUNTRY") && isFieldAllowed(uField.getFullName()) ) {

               StringBuffer origCountry = new StringBuffer(m_location);

               //Insert ORIG type value into country where clause

               typeValueInd = origCountry.indexOf("type_value");
               origCountry.replace(typeValueInd, typeValueInd+10, "'ORIG'");

               //A Country Location Type
               locationTypeInd = origCountry.indexOf("location_type");
               origCountry.replace(locationTypeInd, locationTypeInd+13, "'COUNTRY'");

               //Insert Search value into country where clause

               countryValueInd = origCountry.indexOf("country_value");
               origCountry.replace(countryValueInd, countryValueInd+13, "'"+value+"'");

               parametersQuery.append(origCountry);

          }

          /*  Build a special subquery to find the DEST Country
           */
          else if(fieldCd.equals("DOC_HB-DEST_COUNTRY") && isFieldAllowed(uField.getFullName()) ) {

               StringBuffer destCountry = new StringBuffer(m_location);

               //Insert DEST type value into country where clause

               typeValueInd = destCountry.indexOf("type_value");
               destCountry.replace(typeValueInd, typeValueInd+10, "'DEST'");

               //A Country Location Type
               locationTypeInd = destCountry.indexOf("location_type");
               destCountry.replace(locationTypeInd, locationTypeInd+13, "'COUNTRY'");

               //Insert Search value into country where clause

               countryValueInd = destCountry.indexOf("country_value");
               destCountry.replace(countryValueInd, countryValueInd+13, "'"+value+"'");

               parametersQuery.append(destCountry);

          }
          /* If it isn't a special FIELD_CD then use this
           * generic Java code to build the where clause
           */
          else
          {
            if (isFieldAllowed(uField.getFullName())) {

              if(uField.hasValidFunctionCall())
              {
                parametersQuery.append(" and "+uField.getFunctionCall()+operator);
              }
              else
              {
                parametersQuery.append(" and "+uField.getFullName()+operator);
              }

              if(uField.getType().equals("NUMBER"))
              {
                parametersQuery.append(value+" ");
              }
              else
              {
                parametersQuery.append("'"+value+"' ");
                if (operator.equals(" like ")) {
                   parametersQuery.append("||'%' ");
                }
              }
            }
          }
        } //if validPairValue
      } //try
      catch (NullPointerException npe)
      {
        npe.printStackTrace();
      }
      catch (IndexOutOfBoundsException iobe)
      {
        iobe.printStackTrace();
      }
      catch (SQLException sqle)
      {
        sqle.printStackTrace();
      }
    } //for loop


    parametersQuery.append(buildStatusString(statusBuffer,statusListBuffer,statusIncludeBuffer));

    parametersQuery.append(buildCustRefString(custRefBuffer,custRefValue,match));

    parametersQuery.append(buildDateRange(searchDateType,startDate,endDate));


    // If user in Cust Ref screen and not searching by Reference, return null
    if (searchingByRef)      {
      addRefClauses(query, parametersQuery);
    }
    else if (functionId.equals("314")) {
        query.append(m_nullWhere);
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
  private String DocWhere =  " Where UTIADM.DOCUMENT.DOCUMENT_TYPE = 'HB'";


  private String DocHbJoin = " And UTIADM.DOCUMENT.DOCUMENT_ID  = UTIADM.DOC_HB.DOCUMENT_ID ";


  private String DocMBJoin = " And UTIADM.DOC_DOC_LNK.DOCUMENT_ID = UTIADM.DOC_HB.DOCUMENT_ID "+
                             " And UTIADM.DOC_DOC_LNK.PARENT_DOC_ID = UTIADM.DOC_MB.DOCUMENT_ID ";



  private String ReferenceTable = " UTIADM.REFERENCE, ";

  private String ReferenceFrom = " UTIADM.DOC_LINE_REF_LNK, "+
                                 "       (SELECT A.DOC_LINE_ID, "+
                                 "               A.Inventory_ID, "+
                                 "               B.DOCUMENT_ID   "+
                                 "         FROM  UTIADM.Doc_Line_Inv_Lnk A, "+
                                 "                UTIADM.Doc_Inv_Lnk      B  "+
                                 "       WHERE  A.INVENTORY_ID = B.INVENTORY_ID) POLNK, ";


  private String ReferenceJoin = " And UTIADM.REFERENCE.REFERENCE_ID = UTIADM.DOC_LINE_REF_LNK.REFERENCE_ID "+
                                 " And POLNK.DOC_LINE_ID = UTIADM.DOC_LINE_REF_LNK.DOC_LINE_ID "+
                                 " And POLNK.DOCUMENT_ID = UTIADM.DOCUMENT.DOCUMENT_ID "+
                                 " And UTIADM.REFERENCE.REFERENCE_TYPE_CD Not In ('PO_NO','MBL_NO','MAWB_NO','LOAD_REF','HB_YEAR','HB_NO') ";


  private String m_location =
        " and   UTIADM.DOCUMENT.DOCUMENT_ID in ( "+
        "Select  DLL.DOCUMENT_ID "+
        "From    UTIADM.DOC_LOC_LNK DLL "+
        "Where   DLL.LOC_TYPE_CD = type_value "+ // <--ORIG or DEST
        "and     DLL.LOCATION_ID In ("+
                     "Select  Loc.Location_Id "+
                     "From    UTIADM.LOCATION Loc "+
                     "Connect By Prior Loc.Location_Id = Loc.Location_Parent_Id "+
                     "Start   With Loc.Location_Id = ("+
                               "Select LocS.Location_Id "+
                               "From   UTIADM.LOCATION    LocS "+
                               "Where  LocS.Location_Type = location_type "+ // <--COUNTRY or REGION
                               "And    LocS.uTrac_Cd      = country_value))) "; //<--Search value

 // Include a deliberate false
 private String m_nullWhere = " And 1 = 2";

}
