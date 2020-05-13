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
 * Function to perform "Waybill Images" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the api_config.xml file.
 *
 * @author  Gerhard Potgieter
 */

public class WaybillImages extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public WaybillImages()
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
    if(m_fieldManager.containsTable("IMAGE_INFO@JNB"))

		{
		 query.append(",UAT.HB_IMAGE@JNB ");

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
		if(hasParameter(parameters,"DOC-DOCUMENT_ID"))
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
					if(fieldCd != null && fieldCd.equals("DOC-DOCUMENT_ID"))
					{
						query.append(m_nullWhere);
						return query.toString();
					}
          continue;
        }
            //Check the field_id
            else
            {
			//IF A DOCUMENT.DOCUMENT_ID IS PASSED THROUGH
            if (fieldCd.equals("DOC-DOCUMENT_ID"))
             {

			   			 Field uField = getField(fieldCd);

               if(isFieldAllowed(uField.getFullName()))
               {

			 					query.append("AND HB_IMAGE.IMAGE_ID = IMAGE_INFO.IMAGE_ID "+
								"AND HB_IMAGE.HB_NO = "+
								"(SELECT UTIADM.Query_Pkg.Get_Doc_Ref(DOCUMENT.Document_ID, 'HB_NO', 'HB') FROM UTIADM.DOCUMENT WHERE UTIADM.DOCUMENT.DOCUMENT_ID = '"+value+"') "+
								"AND HB_IMAGE.ORIG_BRANCH = "+
								"(SELECT UTIADM.Query_Pkg.Get_Doc_Loc_CD(Document.Document_ID, 'ORIG', 'BRANCH')FROM UTIADM.DOCUMENT WHERE UTIADM.DOCUMENT.DOCUMENT_ID = '"+value+"') "+
								"AND HB_IMAGE.HB_MODE = "+
								"(SELECT UTIADM.DOC_HB.CAPTURE_MODE FROM UTIADM.DOC_HB,UTIADM.DOCUMENT WHERE UTIADM.DOC_HB.DOCUMENT_ID = UTIADM.DOCUMENT.DOCUMENT_ID AND UTIADM.DOCUMENT.DOCUMENT_ID = '"+value+"') "+
								"AND HB_IMAGE.YEAR = "+
								"(SELECT UTIADM.Query_Pkg.Get_Doc_Ref(DOCUMENT.Document_ID, 'HB_YEAR','HB')FROM UTIADM.DOCUMENT WHERE UTIADM.DOCUMENT.DOCUMENT_ID = '"+value+"') "+
								"AND IMAGE_INFO.CATEGORY = CD_TABLE.LOOKUP_CD(+) "+
								"AND CD_TABLE.TABLE_ID(+) = 'HB_IMAGE_CATEGORY' "+
								"AND WEB_SERVICES.DB_CD = IMAGE_INFO.DB_LOCATION "+
								"AND WEB_SERVICES.SERVICE = 'HB_IMAGE_PATH' "+
	   						" ORDER BY IMAGE_INFO.TIMESTAMP");
			   				}
              }

			//ELSE BUILD GENERIC SELECT
			else{

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
            } //if validPairValue
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
}
    else
		{
		 //If NodeList is missing a value in key-parameter, then return no data
		 query.append(m_nullWhere);
		}
    return query.toString();
  }


  //members
  private String m_where = " WHERE 1 = 1 ";
  // Include a deliberate false
	private String m_nullWhere = " and 1 = 2";

}
