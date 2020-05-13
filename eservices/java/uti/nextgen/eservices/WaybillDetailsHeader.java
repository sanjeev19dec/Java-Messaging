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
 * Function to perform "Waybill Details Header" queries.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author  Wendy Tessendorf
 */

public class WaybillDetailsHeader extends BaseSQLFunction
{
  /**
   * Default constructor
   */

  public WaybillDetailsHeader()
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
    NodeList parameters = doc.getElementsByTagName("parameter");

    createFrom(query);
    query.append(m_where);

    if( m_fieldManager.containsTable("DOC_HB"))
    {
      query.append(DocHbJoin);
    }

    if( m_fieldManager.containsTable("DOC_MB"))
    {
      query.append(DocMBJoin);
    }

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
             if((fieldCd != null) && 
                (fieldCd.equals("DOC_HB-DOCUMENT_ID")))
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

    } //hasParameter()
    else
    {
     //If NodeList is missing a value in key-parameter, then return no data
      query.append(m_nullWhere);
    }

    return query.toString();
  }

  //members
  private String m_where =
        " Where 1 = 1 ";

  private String DocHbJoin = " And UTIADM.DOCUMENT.DOCUMENT_ID  = UTIADM.DOC_HB.DOCUMENT_ID ";

  private String DocMBJoin = " And UTIADM.DOC_DOC_LNK.DOCUMENT_ID = UTIADM.DOC_HB.DOCUMENT_ID "+
                             " And UTIADM.DOC_DOC_LNK.PARENT_DOC_ID = UTIADM.DOC_MB.DOCUMENT_ID ";

 // Include a deliberate false
 private String m_nullWhere = " and 1 = 2";}



