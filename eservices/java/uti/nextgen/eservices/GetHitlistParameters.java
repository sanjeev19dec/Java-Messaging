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
import java.util.logging.*;
import uti.nextgen.tools.*;

/**
 * Function to extract data from the database dynamically based on request
 * input.  The data is extracted using another call to the NextGenFunctions
 * container.
 *
 * @author  Sanjeev Sharma
 */

public class GetHitlistParameters implements Function
{
  /**
   * Default constructor
   */

  public GetHitlistParameters()
  {
  }


  /**
   * Creates a hashtable containing the function id's and associated function
   * names.  The hashtable will be used to decide which sub function to call.
   *
   * @param  name      Function name
   * @param  xmlProps  XML configuration properties.
   * @param  jdbcConf  Database configuration.
   */

  public void init(String name, XMLProperties xmlProps, JDBCConfig jdbcConf)
  {
    try
    {
      m_dom = new DomImpl();

      Element config = xmlProps.getElementByNameAttr("Function",name);
      Element calls = xmlProps.getElement(config,"Calls");

      NodeList callList = xmlProps.getElements(calls,"Call");

      m_callsTable = new Hashtable();

      for(int i = 0; i < callList.getLength(); i++)
      {
        Element call = (Element)callList.item(i);

        m_callsTable.put(xmlProps.getTextNodeValue(call,"FunctionId"),
                         xmlProps.getTextNodeValue(call,"FunctionName"));
      }

      Element genConf = xmlProps.getElement(config,"General");
      m_modeSrchFieldCode = xmlProps.getTextNodeValue(genConf,
                                                      "ModeSearchFieldCode");
      m_alternateMode = xmlProps.getTextNodeValue(genConf,"AlternateMode");
      m_template = xmlProps.getTextNodeValue(genConf,"XMLTemplate");

      m_jdbcResourcePool = NextGenEnv.getResourcePool();
      Element sqlConf = xmlProps.getElement(config,"SQL");
    
      m_hasModeSelect = xmlProps.getTextNodeValue(sqlConf,"HasModeSelect");
      m_getModeSelect = xmlProps.getTextNodeValue(sqlConf,"GetModeSelect");
      m_getFunctionIdSelect = 
                      xmlProps.getTextNodeValue(sqlConf,"GetFunctionIdSelect");
    }
    catch (ConfException ce)
    {
      m_logger.log(Level.SEVERE,"Configuration exception",ce);
    }
  }

 
  /**
   * Returns the value of the parameter with the given name.
   *
   * @param  name  Name of the parameter.
   * @param  doc   Request document.
   *
   * @return  String containing the value of the named parameter.
   */

  private String getParameterValue(String name,Document doc)
  {
    NodeList params = 
                 doc.getDocumentElement().getElementsByTagName("parameter"); 

    for(int i = 0; i < params.getLength(); i++)
    {
      Element parameter = (Element)params.item(i);
      String paramName = m_dom.getTextNodeValue(
                                        m_dom.getElement("field_cd",parameter));

      if((paramName != null) && (paramName.equals(name)))
      {
        return m_dom.getTextNodeValue(m_dom.getElement("value",parameter));
      }
    }

    return null;
  }


  /**
   * Returns a function id associated with the given function id and transport
   * mode.
   *
   * @param  functionId     Function id received in request document.
   * @param  transportMode  Transport mode
   * @param  conn  Database connection.
   *
   * @return  String containing function id.
   *
   * @throws  SQLException if an exception occurs communicating to the database.
   */

  private String getFunctionId(String functionId, 
                               String transportMode,
                               Connection conn) throws SQLException
  {
    String newId = null;
    PreparedStatement stmnt = conn.prepareStatement(m_getFunctionIdSelect);

    stmnt.setObject(1,new Integer(functionId));
    stmnt.setObject(2,transportMode);

    ResultSet rs = stmnt.executeQuery();

    if(rs.next())
    {
      newId = rs.getString("member_service_id");
    }

    rs.close();
    stmnt.close();

    return newId; 
  }


  /**
   * Determines the transport mode of the given request document.
   *
   * @param  doc   Request document.
   * @param  conn  Database connection.
   *
   * @return String containing the transport mode of this request document.
   *
   * @throws  SQLException if an exception occurs communicating to the database.
   */

  private String getMode(Document doc,Connection conn) throws SQLException
  {
    String mode = null;
    String docId = getParameterValue(m_modeSrchFieldCode,doc);

    if(docId != null)
    {
      PreparedStatement stmnt = conn.prepareStatement(m_getModeSelect); 

      stmnt.setObject(1,docId);
      ResultSet rs = stmnt.executeQuery();

      if(rs.next())
      {
        mode = rs.getString("capture_mode");
      }

      rs.close();
      stmnt.close();
    }
  
    return mode; 
  }


  /**
   * Determines if the given function id is transport mode specific.
   *
   * @param  functionId  Given function id.
   * @param  conn        Database connection.
   *
   * @return  boolean Indicating whether or not the function id is transport 
   *          mode specific
   *
   * @throws  SQLException if an exception occurs communicating to the database.
   */

  private boolean hasMode(String functionId,Connection conn) throws SQLException
  {
    boolean bResult = false;
    PreparedStatement stmnt = conn.prepareStatement(m_hasModeSelect); 

    stmnt.setObject(1,new Integer(functionId));
    ResultSet rs = stmnt.executeQuery();

    if(rs.next())
    {
      int count = rs.getInt("scount");

      if(count > 0)
      {
        bResult = true;
      }
    }

    rs.close();
    stmnt.close();

    return bResult;
  } 


  /**
   * Determines a new function id to use if the given request document is 
   * transport mode specific.
   *
   * @param  fucntionId  Receive function id.
   * @param  doc         Request document.
   * @param  conn        Database connection to use.
   *
   * @throws SQLException if the function id cannot be determined.
   */

  private String getFunctionIdForMode(String functionId, 
                                      Document doc,
                                      Connection conn) throws SQLException
  {
    boolean hasMode = hasMode(functionId,conn);

    if(hasMode)
    {
      String newId = null;
      String mode = getMode(doc,conn); 

      if(mode != null)
      {
        newId = getFunctionId(functionId,mode,conn);

        if((newId == null) && (!(mode.equals("A"))))
        {
          newId = getFunctionId(functionId,m_alternateMode,conn);
        }
   
        return newId;
      }
      else
      {
        return functionId;
      }
    }
    else
    {
      return functionId;
    }
  }


  /**
   * Extracts the function_id element from the given document and decides which
   * sub function to call.
   *
   * @param  doc  Document containing parameters for select statement.
   * @param  functions  The NextGenFunctions container this function lives in.
   */

  public String executeForString(Document doc,
                                 NextGenFunctions functions) throws APIException
  {
    Connection conn = null;

    try
    {
      try
      {
        conn = (Connection)m_jdbcResourcePool.getResource();
      }
      catch (ResourcePoolException rpe)
      {
        m_logger.log(Level.SEVERE,
                     "Unable to obtain connection from ResourcePool");
        throw new APIException(rpe);
      }

      String functionId = m_dom.getTextNodeValue("function_id",doc);
      m_logger.log(Level.INFO,"Received function_id: "+functionId);
    
      functionId = getFunctionIdForMode(functionId,doc,conn); 
      m_logger.log(Level.INFO,"Now using function_id: "+functionId);

      m_dom.setTextNodeValue("function_id",functionId,doc);

      String functionName = (String)m_callsTable.get(functionId);
      m_logger.log(Level.INFO,"Attempting to execute function: "+functionName);

      Document outDoc = null;

      if(functionName != null)
      {
        Function function = functions.getFunction(functionName);
        outDoc = function.executeForDocument(doc,functions);
      }
      else
      {
        outDoc = executeForDocument(doc,functions);
      }

      m_logger.log(Level.INFO,"Successfully executed function: "+functionName);

      return m_dom.getXMLStr(outDoc);
    }
    catch (SQLException sqle)
    {
      throw new APIException(sqle);
    }
    finally
    {
      try
      {
        m_jdbcResourcePool.releaseResource(conn);
        m_logger.log(Level.INFO,"Releasing connection back to pool");
      }
      catch (ResourcePoolException rpe)
      {
        m_logger.log(Level.WARNING,
                     "Unable to release connection back to ResourcePool",rpe);
      }
    }
  }


  /**
   * Returns an empty hitlist if no matching function could be determined for
   * the incoming request.
   */

  public Document executeForDocument(Document doc,
                                     NextGenFunctions functions)
                                                       throws APIException
  {
    Document template = NextGenEnv.getTemplate(m_template); 
    String reqType = m_dom.getTextNodeValue("req_type",doc);
    Node respTypeElement = m_dom.getElement("resp_type",template);

    if(respTypeElement.hasChildNodes())
    {
      m_dom.setTextNodeValue(respTypeElement,reqType);
    }
    else
    {
      respTypeElement.appendChild(template.createTextNode(reqType));
    }

    return template;
  } 


  //members
  private static Logger m_logger =
                Logger.getLogger("uti.nextgen.eservices.GetHitlistParameters");

  private DomImpl m_dom = null;
  private String m_template = null;
  private Hashtable m_callsTable = null;
  private String m_hasModeSelect = null;
  private String m_getModeSelect = null;
  private String m_getFunctionIdSelect = null;
  private String m_alternateMode = null;
  private String m_modeSrchFieldCode = null; 
  private JDBCResourcePool m_jdbcResourcePool = null;
}
