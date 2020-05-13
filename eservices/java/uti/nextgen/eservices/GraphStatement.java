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
 * Function to perform "Graph statement" query.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author  Wendy Tessendorf
 */

public class GraphStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public GraphStatement()
  {
  }


  /**
   * Initialise all our where clauses.
   */

  private void initWhereClauses()
  {

    firstSelect1 = new StringBuffer("select 'On Time','Bar','445890',"+
                                     "bwadm.Scv_Pkg.SCV_Metrics(null, 'On Order','On Time', ref_type1, ref_value1 ) "+
                                     ",0,0,0,warehouse_qty(),0,0,0 from Dual ");

    secondSelect1 = new StringBuffer("UNION select 'Late','Bar','Red',"+
                                    "bwadm.Scv_Pkg.SCV_Metrics(null, 'On Order','Late', ref_type1, ref_value1),"+
                                    "0,0,0,0,0,0,0 from Dual ");

    orderBy = new StringBuffer("order by 1 desc");
  }


  /**
   * Creates the query.
   */

  protected String buildQuery(Document doc)
  {
    initWhereClauses();

    StringBuffer query = new StringBuffer();
    NodeList parameters = doc.getElementsByTagName("parameter");
    int ind = 0;
    int indRefType1 = 0;
    int indRefVal1 = 0;

    String referenceValue = "";
    String referenceType  = "";
    String locationId  = "";


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
          Field uField = getField(fieldCd);

          if(isFieldAllowed(uField.getFullName()))
          {
            if(fieldCd.equals("REF-REFERENCE_VALUE")){

               referenceValue = "'"+value+"'";
            }
            else if (fieldCd.equals("REF-REFERENCE_TYPE_CD")){

               referenceType = "'"+value+"'";
            }
            else if (fieldCd.equals("INV--CURR_LOCATION_ID")){

               locationId = value;
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
    }//for loop



    /* If any of the search parameters are not used, |
     | set their values = null for the function call */
    if (referenceType.length() == 0) {
        referenceType = "null";
    }
    if (referenceValue.length() == 0 ) {
        referenceValue = "null";
    }
    if (locationId.length() == 0) {
        locationId = "null";
    }


    //Insert the necessary parameter values into the function calls
    try
    {
      indRefType1 = firstSelect1.indexOf("ref_type1");
      firstSelect1.replace(indRefType1,indRefType1+9, referenceType);

      indRefVal1 = firstSelect1.indexOf("ref_value1");
      firstSelect1.replace(indRefVal1,indRefVal1+10, referenceValue);

      indRefType1 = 0;
      indRefVal1 = 0;

      indRefType1 = secondSelect1.indexOf("ref_type1");
      secondSelect1.replace(indRefType1,indRefType1+9, referenceType);

      indRefVal1 = secondSelect1.indexOf("ref_value1");
      secondSelect1.replace(indRefVal1,indRefVal1+10, referenceValue);


      ind = firstSelect1.indexOf("warehouse_qty(");
      firstSelect1.insert(ind+14, referenceValue+","+referenceType+","+locationId);

    }
    catch (IndexOutOfBoundsException npe)
    {
      npe.printStackTrace();
    }


    query.append(firstSelect1);
    query.append(secondSelect1);

    query.append(orderBy);
    return query.toString();
  }


  //members
  private StringBuffer orderBy = null;
  private StringBuffer firstSelect1 = null;
  private StringBuffer secondSelect1 = null;

}
