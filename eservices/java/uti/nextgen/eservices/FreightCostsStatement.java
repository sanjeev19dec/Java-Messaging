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
 * Function to perform "Freight Costs statement" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author Wendy Tessendorf
 */

public class FreightCostsStatement extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public FreightCostsStatement()
  {
  }


  /**
   * Creates the query.
   */

  protected String buildQuery(Document doc)
  {
    StringBuffer query = new StringBuffer();
    StringBuffer whereClause = new StringBuffer();
    NodeList parameters = doc.getElementsByTagName("parameter");

    if (hasParameter(parameters,"DOC_HB-DOCUMENT_ID"))
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
            continue;
          }
          else
          {
            Field uField = getField(fieldCd);

            if(isFieldAllowed(uField.getFullName()))
            {
              if(uField.hasValidFunctionCall())
              {
                whereClause.append("and "+uField.getFunctionCall()+" = ");
              }
              else
              {
                whereClause.append("and "+uField.getFullName()+" = ");
              }

              if(uField.getType().equals("NUMBER"))
              {
                whereClause.append(value+" ");
              }
              else
              {
                whereClause.append("'"+value+"' ");
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

      whereClause.replace(0,3, "");

      query.append(firstSelect);
      query.append(whereClause);
      query.append(secondSelect);
      query.append(whereClause);
      query.append(totalsSelect);
      query.append(whereClause);

      System.out.println("********* NORMAL MO");

    } //hasParameter()
    else
    {
     //If NodeList is missing a value in key-parameter, then return no data
      query.append(firstSelect);
      System.out.println("********* appending the null where now");
      query.append(m_nullWhere);
    }

    return query.toString();
  }


  //members
  private String firstSelect =
  "select 'FREIGHT (LEG 1)', "+
        "UTIADM.Query_Pkg.Get_Doc_Journey_Info(UTIADM.DOC_HB.DOCUMENT_ID, 1, 'CHARGE_C') "+
        "Freight_Value_Leg_1_C,  "+
        "UTIADM.Query_Pkg.Get_Doc_Journey_Info(UTIADM.DOC_HB.DOCUMENT_ID, 1, 'CHARGE_P') "+
        "Freight_Value_Leg_1_P, "+
        "UTIADM.Query_Pkg.Get_Doc_Journey_Info(UTIADM.DOC_HB.DOCUMENT_ID, 1, 'CURRENCY') "+
        "Freight_Leg_1_Curr "+
   "from UTIADM.DOC_HB "+
   "where ";


 private String secondSelect =
 "UNION "+
 "select 'FREIGHT (LEG 2)', "+
        "UTIADM.Query_Pkg.Get_Doc_Journey_Info(UTIADM.DOC_HB.DOCUMENT_ID, 2, 'CHARGE_C') "+
        "Freight_Value_Leg_2_C, "+
        "UTIADM.Query_Pkg.Get_Doc_Journey_Info(UTIADM.DOC_HB.DOCUMENT_ID, 2, 'CHARGE_P') "+
        "Freight_Value_Leg_2_P, "+
        "UTIADM.Query_Pkg.Get_Doc_Journey_Info(UTIADM.DOC_HB.DOCUMENT_ID, 2, 'CURRENCY') "+
        "Freight_Leg_2_Curr "+
 "from UTIADM.DOC_HB "+
 "where ";


 private String totalsSelect =
 "UNION "+
 "select 'Total Charges', "+
         "TO_CHAR(UTIADM.Query_Pkg.Get_Doc_Journey_Info(UTIADM.DOC_HB.DOCUMENT_ID, 1, 'CHARGE_C')+ "+
         "UTIADM.Query_Pkg.Get_Doc_Journey_Info(UTIADM.DOC_HB.DOCUMENT_ID, 2, 'CHARGE_C')), "+
         "TO_CHAR(UTIADM.Query_Pkg.Get_Doc_Journey_Info(UTIADM.DOC_HB.DOCUMENT_ID, 1, 'CHARGE_P')+ "+
         "UTIADM.Query_Pkg.Get_Doc_Journey_Info(UTIADM.DOC_HB.DOCUMENT_ID, 1, 'CHARGE_P')), "+
         "UTIADM.Query_Pkg.Get_Doc_Journey_Info(UTIADM.DOC_HB.DOCUMENT_ID, 1, 'CURRENCY')  "+
 "from UTIADM.DOC_HB "+
 "where ";

  // Include a deliberate false
  private String m_nullWhere = " 1 = 2";

}
