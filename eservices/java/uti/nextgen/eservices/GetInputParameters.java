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
import org.w3c.dom.*;
import java.util.logging.*;
import uti.nextgen.tools.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;


/**
 * Function to extract data from default_input table.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author  Sanjeev Sharma
 */

public class GetInputParameters implements Function
{
  /**
   * Default constructor
   */

  public GetInputParameters()
  {
  }


  /**
   * Initialises the XML template document and select statement.
   *
   * @param  name      Function name
   * @param  xmlProps  XML configuration properties.
   * @param  jdbcConf  Database configuration.
   */

  public void init(String name, XMLProperties xmlProps, JDBCConfig jdbcConf)
  {
    try
    {
      m_name = name;
	  m_xmlProps = xmlProps;
      m_jdbcResourcePool = NextGenEnv.getResourcePool();
      m_dom = new DomImpl();

      Element config = xmlProps.getElementByNameAttr("Function",name);
      Element sql = xmlProps.getElement(config,"SQL");

      m_select = xmlProps.getTextNodeValue(sql,"Select");
      m_templateName = xmlProps.getTextNodeValue(config,"XMLTemplate");
    }
    catch (ConfException ce)
    {
      ce.printStackTrace();
    }
  }


  /**
   * Returns a clone of the input_field element.
   *
   * @return Node
   */

  private Node getInputFieldClone()
  {
    Node inputField = m_dom.getElement("input_field",m_template);
    return inputField.cloneNode(true);
  }

  /**
   * Returns a clone of the default element.
   *
   * @return Node
   */

  private Node getDefaultClone()
  {
    Node deFault = m_dom.getElement("default",m_template);
    return deFault.cloneNode(true);
  }

  /**
   * Returns a clone of the default element.
   *
   * @return Node
   */

  private Node getValueClone()
  {
    Node dValue = m_dom.getElement("value",m_template);
    return dValue.cloneNode(false);
  }

  /**
   * Executes the select statement and populates the template.
   *
   * @param  doc  Document containing parameters for select statement.
   * @param  functions  The NextGenFunctions container this function lives in.
   */

  public String executeForString(Document doc,
                                 NextGenFunctions functions) throws APIException
  {
    try
    {
      m_template = NextGenEnv.getTemplate(m_templateName);
      Node inputScreen = m_dom.getElement("inputscreen",m_template);

	  Node urequest = m_dom.getElement("urequest", doc);
      String functionId = m_dom.getTextNodeValue("function_id",doc);
	  String profileId = m_dom.getTextNodeValue("profile_id",doc);
	  String participantId = m_dom.getTextNodeValue("participant_id",doc);

      NodeList parameterList = m_dom.getElements("parameter",(Element)urequest);
      HashMap field_value = new HashMap();

      for(int i = 0; i < parameterList.getLength(); i++)
      {
        Element parameter = (Element)parameterList.item(i);
        String fldCode = m_dom.getTextNodeValue("field_cd",parameter);
        String value = m_dom.getTextNodeValue("value",parameter);
		//if(fldCode != null)
        field_value.put(fldCode, value);
      }

      Connection conn = (Connection)m_jdbcResourcePool.getResource();
      PreparedStatement statement = conn.prepareStatement(m_select);

      statement.setObject(1,new Integer(functionId));
      statement.setObject(2,new Integer(profileId));
      statement.setObject(3,new Integer(participantId));

      statement.setObject(4,new Integer(functionId));
      statement.setObject(5,new Integer(profileId));
      statement.setObject(6,new Integer(participantId));

      ResultSet rs = statement.executeQuery();
      String rsVal = null;
      String serviceId = null;
      String displayType = null;
      String inputDefVal = null;
      String defaultInputDefVal = null;

      while(rs.next())
      {
        Element inputField = (Element)getInputFieldClone();

        rsVal = rs.getString("on_focus_event");

        if(rsVal != null)
        {
          Node onFocus = m_dom.getElement("on_focus",inputField);
          onFocus.appendChild(m_template.createTextNode(rsVal));
        }

        rsVal = rs.getString("on_blur_event");

        if(rsVal != null)
        {
          Node onBlur = m_dom.getElement("on_blur",inputField);
          onBlur.appendChild(m_template.createTextNode(rsVal));
        }

        rsVal = rs.getString("on_change_event");

        if(rsVal != null)
        {
          Node onChange = m_dom.getElement("on_change",inputField);
          onChange.appendChild(m_template.createTextNode(rsVal));
        }

        rsVal = rs.getString("on_mouse_over_event");

        if(rsVal != null)
        {
          Node onMouseOver = m_dom.getElement("on_mouse_over",inputField);
          onMouseOver.appendChild(m_template.createTextNode(rsVal));
        }

        rsVal = rs.getString("on_mouse_out_event");

        if(rsVal != null)
        {
          Node onMouseOut = m_dom.getElement("on_mouse_out",inputField);
          onMouseOut.appendChild(m_template.createTextNode(rsVal));
        }


        rsVal = rs.getString("display_prompt");

        if(rsVal != null)
        {
          Node fldPrompt = m_dom.getElement("fld_prompt",inputField);
          fldPrompt.appendChild(m_template.createTextNode(rsVal));
        }

        rsVal = rs.getString("display_width");

        if(rsVal != null)
        {
          Node fldWidth = m_dom.getElement("fld_width",inputField);
          fldWidth.appendChild(m_template.createTextNode(rsVal));
        }

		boolean singleDisplayType = true;
        displayType = rs.getString("display_type");

        if(displayType != null)
        {
          Node fldType = m_dom.getElement("fld_type",inputField);
          fldType.appendChild(m_template.createTextNode(displayType));
          if(displayType.equals("SELECT") || displayType.equals("MULTI-SELECT")
          				|| displayType.equals("RADIO") || displayType.equals("SORT"))
          	 singleDisplayType = false;
		}


        rsVal = rs.getString("field_cd");

        if(rsVal != null)
        {
          Node fldId = m_dom.getElement("fld_cd",inputField);
          fldId.appendChild(m_template.createTextNode(rsVal));
        }

		boolean fieldCode = false;
		boolean fieldVal = false;

	  	Set fldCodes = field_value.keySet();

	    for (Iterator it = fldCodes.iterator(); it.hasNext();)
	    {
		  String fldCode = (String) it.next();
		  if(fldCode != null && fldCode.equals(rsVal)){
		  	  inputDefVal = (String) field_value.get(fldCode);
		  	  fieldCode = true;
		  	  if(inputDefVal != null)
		  	  	 fieldVal = true;
		  }
	    }

	    defaultInputDefVal = rs.getString("default_value");

        serviceId = rs.getString("lookup_service_id");
		// For Single Display Type
		if((!fieldCode && !fieldVal) && singleDisplayType)
		{
			if(serviceId != null)
			{
				Node errInd = m_dom.getElement("err_ind",inputField);
				Vector defaultValues[] = getDefaultValues(serviceId);
				int len = defaultValues.length;

				if(!defaultValues[0].isEmpty())
				{
					for (int j=0; j < defaultValues[0].size(); j++)// For loop to populate all default values
					{
					   Element deFault = (Element)getDefaultClone();
					   if(defaultValues[0].get(j) != null)
					   {
						   Node dValue = m_dom.getElement("value",deFault);
						   dValue.appendChild(m_template.createTextNode((String)defaultValues[0].get(j)));
					   }
					   if(len > 1 && defaultValues[1].get(j) != null)
					   {
						   Node dValue = m_dom.getElement("display_name",deFault);
						   dValue.appendChild(m_template.createTextNode((String)defaultValues[1].get(j)));
					   }

					   inputField.insertBefore(deFault,errInd);
					}
					Node origDefault = m_dom.getElement("default",inputField);
					inputField.removeChild(origDefault);
				}//End if
				else // Else for empty list of default values
				{
				  Node def = m_dom.getElement("default",inputField);
				  Node dValue = m_dom.getElement("value",(Element)def);
				  def.removeChild(dValue);
				}

			}//End if
			else // Else for lookup serviceId == null
			{
				if(defaultInputDefVal != null)
				{
				  Node def = m_dom.getElement("default",inputField);
				  Node dValue = m_dom.getElement("value",(Element)def);
				  dValue.appendChild(m_template.createTextNode(defaultInputDefVal));
				}
				else
				{
				  Node def = m_dom.getElement("default",inputField);
				  Node dValue = m_dom.getElement("value",(Element)def);
				  def.removeChild(dValue);
				}
			}
		}//End if
		else
		{
			Node def = m_dom.getElement("default",inputField);
			Node dValue = m_dom.getElement("value",(Element)def);
			if(inputDefVal != null)
				dValue.appendChild(m_template.createTextNode(inputDefVal));
			else
				dValue.appendChild(m_template.createTextNode(""));
		}


		//For Select, Multi-Select and Radio Display Type
		if(!singleDisplayType && (displayType.equals("SELECT") || displayType.equals("MULTI-SELECT")
          				|| displayType.equals("RADIO")))
		{
			if(serviceId != null)  //(!fieldCode && !fieldVal)
			{
				Node errInd = m_dom.getElement("err_ind",inputField);
				Vector defaultValues[] = getDefaultValues(serviceId);
				int len = defaultValues.length;

				if(!defaultValues[0].isEmpty())
				{
					for (int j=0; j < defaultValues[0].size(); j++)// For loop to populate all default values
					{
					   Element deFault = (Element)getDefaultClone();
					   String curDefaultVal = (String)defaultValues[0].get(j);
					   if(defaultValues[0].get(j) != null)
					   {
						   Node dValue = m_dom.getElement("value",deFault);
						   dValue.appendChild(m_template.createTextNode(curDefaultVal));
					   }
					   if(len > 1 && defaultValues[1].get(j) != null)
					   {
						   Node dValue = m_dom.getElement("display_name",deFault);
						   dValue.appendChild(m_template.createTextNode((String)defaultValues[1].get(j)));
					   }
					   if(displayType != null  && (displayType.equals("SELECT") || displayType.equals("MULTI-SELECT") || displayType.equals("RADIO")))
					   {
						  if (!fieldCode && !fieldVal)
						  {
							  if(defaultInputDefVal != null && defaultInputDefVal.equals(curDefaultVal))
							  {
								Node selected = m_dom.getElement("selected",deFault);
								selected.appendChild(m_template.createTextNode("Y"));
							  }
						  }
						  else
						  {
							  if(inputDefVal != null && inputDefVal.equals(curDefaultVal))
							  {
								Node selected = m_dom.getElement("selected",deFault);
								selected.appendChild(m_template.createTextNode("Y"));
							  }
						  }
					   }

					   inputField.insertBefore(deFault,errInd);
					}
					Node origDefault = m_dom.getElement("default",inputField);
					inputField.removeChild(origDefault);
				}//End if
				else // Else for empty list of default values
				{
					if(defaultInputDefVal != null)
					{
					  Node def = m_dom.getElement("default",inputField);
					  Node dValue = m_dom.getElement("value",(Element)def);
					  dValue.appendChild(m_template.createTextNode(defaultInputDefVal));
					}
					else
					{
					  Node def = m_dom.getElement("default",inputField);
					  Node dValue = m_dom.getElement("value",(Element)def);
					  def.removeChild(dValue);
					}
				}

			}//End if
			else // Else for lookup serviceId == null
			{
				if(defaultInputDefVal != null)
				{
				  Node def = m_dom.getElement("default",inputField);
				  Node dValue = m_dom.getElement("value",(Element)def);
				  dValue.appendChild(m_template.createTextNode(defaultInputDefVal));
				}
				else
				{
				  Node def = m_dom.getElement("default",inputField);
				  Node dValue = m_dom.getElement("value",(Element)def);
				  def.removeChild(dValue);
				}
			}
		}//End if

		//For SORT Display Type
		if(!singleDisplayType && (displayType.equals("SORT")))
		{
			if(serviceId != null)
			{
				Node errInd = m_dom.getElement("err_ind",inputField);
				Vector defaultValues[] = getSortedDefaultValues(serviceId);
				int len = defaultValues.length;

				if(!defaultValues[0].isEmpty())
				{
					for (int j=0; j < defaultValues[0].size(); j++)// For loop to populate all default values
					{
					   Element deFault = (Element)getDefaultClone();
					   String curDefaultVal = defaultValues[0].get(j).toString();
					   if(defaultValues[0].get(j) != null)
					   {
						   Node dValue = m_dom.getElement("value",deFault);
						   dValue.appendChild(m_template.createTextNode(curDefaultVal));
					   }
					   if(len > 1 && defaultValues[1].get(j) != null)
					   {
						   Node dValue = m_dom.getElement("display_name",deFault);
						   dValue.appendChild(m_template.createTextNode((String)defaultValues[1].get(j)));
					   }
					   if(displayType != null  && (displayType.equals("SELECT") || displayType.equals("MULTI-SELECT") || displayType.equals("RADIO")))
					   {
						  if(inputDefVal != null && inputDefVal.equals(curDefaultVal))
						  {
						  	Node selected = m_dom.getElement("selected",deFault);
						  	selected.appendChild(m_template.createTextNode("Y"));
						  }
					   }

					   inputField.insertBefore(deFault,errInd);
					}
					Node origDefault = m_dom.getElement("default",inputField);
					inputField.removeChild(origDefault);
				}//End if
				else // Else for empty list of default values
				{
				  Node def = m_dom.getElement("default",inputField);
				  Node dValue = m_dom.getElement("value",(Element)def);
				  def.removeChild(dValue);
				}

			}//End if
			else // Else for lookup serviceId == null
			{
				if(defaultInputDefVal != null)
				{
				  Node def = m_dom.getElement("default",inputField);
				  Node dValue = m_dom.getElement("value",(Element)def);
				  dValue.appendChild(m_template.createTextNode(defaultInputDefVal));
				}
				else
				{
				  Node def = m_dom.getElement("default",inputField);
				  Node dValue = m_dom.getElement("value",(Element)def);
				  def.removeChild(dValue);
				}
			}
		}//End if

        inputScreen.appendChild(inputField);
      }

      rs.close();
      statement.close();

      Node origInputField = m_dom.getElement("input_field",m_template);
      inputScreen.removeChild(origInputField);

      m_jdbcResourcePool.releaseResource(conn);

      return m_dom.getXMLStr(m_template);
    }
    catch (SQLException sqle)
    {
      throw new APIException(sqle);
    }
    catch (ConfException ce)
    {
      throw new APIException(ce);
    }
    catch (ResourcePoolException rpe)
    {
      throw new APIException(rpe);
    }
  }


  /**
   * Executes the select statement for lookup_service_id and return the values.
   *
   * @param  serviceId  Value of lookup_service_id to execute corresponding select.
   */
  public Vector[] getDefaultValues(String serviceId) throws APIException
  {
    try
    {
      Element config = m_xmlProps.getElementByNameAttr("Function",m_name);
      Element defaultcalls = m_xmlProps.getElement(config,"DefaultCalls");

	  StringBuffer SQLSelect = new StringBuffer("SQLSelect");
	  SQLSelect.append(serviceId);

      String m_defaultselect = m_xmlProps.getTextNodeValue(defaultcalls,SQLSelect.toString());

	  Connection conn = (Connection)m_jdbcResourcePool.getResource();
      Statement statement = conn.createStatement();

      ResultSet rs = statement.executeQuery(m_defaultselect);
      ResultSetMetaData rsmd = rs.getMetaData();

	  int nNumberOfColumns = rsmd.getColumnCount();
	  int nNumberOfRows = 0;

	  Vector m_fieldDataVector[] = new Vector[nNumberOfColumns];

      while (rs.next()) // loop through each column (field), storing the column data
      {
         for ( int i = 0; i < nNumberOfColumns; i++ )
         {
            if ( nNumberOfRows == 0 )
            {
               m_fieldDataVector[i] = new Vector();
            }
            m_fieldDataVector[i].insertElementAt(rs.getString(i+1), nNumberOfRows);
         }
         nNumberOfRows++ ;
      }

      rs.close();
      statement.close();

      m_jdbcResourcePool.releaseResource(conn);

     return m_fieldDataVector;

	}
    catch(ConfException confe)
    {
      confe.printStackTrace();
      return null;
    }
    catch (SQLException sqle)
    {
      throw new APIException(sqle);
    }
    catch (ResourcePoolException rpe)
    {
      throw new APIException(rpe);
    }
  }


  /**
   * Executes the select statement for lookup_service_id and return the sorted values.
   *
   * @param  serviceId  Value of lookup_service_id to execute corresponding select.
   */
  public Vector[] getSortedDefaultValues(String serviceId) throws APIException
  {
    try
    {
      Element config = m_xmlProps.getElementByNameAttr("Function",m_name);

      String dispNameSelect = m_xmlProps.getTextNodeValue(config,"DisplayName");

      Connection conn = (Connection)m_jdbcResourcePool.getResource();
      PreparedStatement statement = conn.prepareStatement(dispNameSelect);
      statement.setObject(1,new Integer(serviceId));

      ResultSet rs = statement.executeQuery();

	  int nNumberOfColumns = 2;
	  int nNumberOfRows = 0;

	  Vector m_fieldDataVector[] = new Vector[nNumberOfColumns];

      while (rs.next()) // loop through each column (field), storing the column data
      {
		if ( nNumberOfRows == 0 )
		{
		   for ( int j = 0; j < nNumberOfColumns; j++ )
		   {
			   m_fieldDataVector[j] = new Vector();
			   m_fieldDataVector[j].insertElementAt(new String(""), 0);
		   }
		   nNumberOfRows++ ;
		}
		m_fieldDataVector[1].insertElementAt(rs.getString(1), nNumberOfRows);
        nNumberOfRows++ ;
      }

      for ( int i = 1; i < nNumberOfRows; i++ )
      {
		 m_fieldDataVector[0].insertElementAt(new Integer(i), i);
	  }

      rs.close();
      statement.close();

      m_jdbcResourcePool.releaseResource(conn);

     return m_fieldDataVector;

	}
    catch(ConfException confe)
    {
      confe.printStackTrace();
      return null;
    }
    catch (SQLException sqle)
    {
      throw new APIException(sqle);
    }
    catch (ResourcePoolException rpe)
    {
      throw new APIException(rpe);
    }
  }


  /**
   * Not implemented.
   */

  public Document executeForDocument(Document doc,
                                     NextGenFunctions functions)
                                                      throws APIException
  {
    return null;
  }


  //members
  private static Logger m_logger =
                   Logger.getLogger("uti.nextgen.eservices.GetInputParameters");

  private String m_name = null;
  private DomImpl m_dom = null;
  private String m_select = null;
  private Document m_template = null;
  private String m_templateName = null;
  private JDBCResourcePool m_jdbcResourcePool = null;
  private XMLProperties m_xmlProps = null;

}
