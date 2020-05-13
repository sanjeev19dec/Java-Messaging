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

/**
 * Function to extract data from the uservices table.
 *
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author  Sanjeev Sharma
 */

public class GetDetailParameters implements Function
{
  /**
   * Default constructor
   */

  public GetDetailParameters()
  {
  }


  /**
   * Initialises the the select statement and XML template name.
   *
   * @param  name  Function name.
   * @param  xmlProps  XML configuration properties.
   * @param  jdbcConf  Data configuration properties.
   */

  public void init(String name, XMLProperties xmlProps, JDBCConfig jdbcConf)
  {
    try
    {
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
   * Returns a clone of the uresponse element.
   *
   * @return Node
   */

  private Node getUresponseClone()
  {
    Node uresponse = m_dom.getElement("uresponse",m_template);
    return uresponse.cloneNode(true);
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
      Node uresponseRoot = m_dom.getElement("uResponseRoot",m_template);

      String functionId = m_dom.getTextNodeValue("function_id",doc);

      Connection conn = (Connection)m_jdbcResourcePool.getResource();
      PreparedStatement statement = conn.prepareStatement(m_select);

      statement.setObject(1,new Integer(functionId));
      ResultSet rs = statement.executeQuery();
      String rsVal = null;

      while(rs.next())
      {
        Element uresponse = (Element)getUresponseClone();

        rsVal = rs.getString("service_id");

        if(rsVal != null)
        {
          Node funcId = m_dom.getElement("function_id",uresponse);
          funcId.appendChild(m_template.createTextNode(rsVal));
        }

        rsVal = rs.getString("request_type");

        if(rsVal != null)
        {
          Node respType = m_dom.getElement("resp_type",uresponse);
          respType.appendChild(m_template.createTextNode(rsVal));
        }

        uresponseRoot.appendChild(uresponse);
      }

      rs.close();
      statement.close();

      Node origResponse = m_dom.getElement("uresponse",m_template);
      uresponseRoot.removeChild(origResponse);

      m_jdbcResourcePool.releaseResource(conn);

      return m_dom.getXMLStr(m_template);
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
                  Logger.getLogger("uti.nextgen.eservices.GetDetailParameters");

  private DomImpl m_dom = null;
  private String m_select = null;
  private Document m_template = null;
  private String m_templateName = null;
  private JDBCResourcePool m_jdbcResourcePool = null;
}
