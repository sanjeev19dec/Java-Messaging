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
 * Function to extract data from default_tree table.
 * <p>
 * The extracted data is used to populate the XML template specified by the
 * XMLTemplate configuration element in the eservices_config.xml file.
 *
 * @author  Sanjeev Sharma
 */

public class GetTreeParameters implements Function
{
  /**
   * Default constructor
   */

  public GetTreeParameters()
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
      m_jdbcResourcePool = NextGenEnv.getResourcePool();
      m_dom = new DomImpl();

      Element config = xmlProps.getElementByNameAttr("Function",name);
      Element sql = xmlProps.getElement(config,"SQL");

      m_isNullSelect = xmlProps.getTextNodeValue(sql,"IsNullSelect");
      m_nullSelect = xmlProps.getTextNodeValue(sql,"NullSelect");

      m_templateName = xmlProps.getTextNodeValue(config,"XMLTemplate");
    }
    catch (ConfException ce)
    {
      ce.printStackTrace();
    }
  }


  /**
   * Returns a clone of the node element.
   *
   * @return Node
   */

  private Node getNodeClone()
  {
    Node node = m_dom.getElement("node",m_template);
    return node.cloneNode(true);
  }


  /**
   * Recursively generates the XML document with the results from the
   * performed SQL queries.
   *
   * @param  conn        Database connection.
   * @param  parent      Parent node to append node elements to.
   * @param  functionId  Parameter used in query.
   * @param  nodeId      Parameter used in query
   *
   * @throws  SQLException
   */

  private void recurse(Connection conn,
                       Node parent,
                       Integer functionId,
                       Integer nodeId) throws SQLException
  {
    PreparedStatement statement = null;

    if(nodeId == null)
    {
      statement = conn.prepareStatement(m_isNullSelect);
    }
    else
    {
      statement = conn.prepareStatement(m_nullSelect);
      statement.setObject(2,nodeId);
    }

    statement.setObject(1,functionId);

    ResultSet rs = statement.executeQuery();
    String rsVal = null;

    while(rs.next())
    {
      Element node = (Element)getNodeClone();

      rsVal = rs.getString("display_value");

      if(rsVal != null)
      {
        Node label = m_dom.getElement("label",node);
        label.appendChild(m_template.createTextNode(rsVal));
      }

      parent.appendChild(node);
      recurse(conn,node,functionId,new Integer(rs.getInt("node_id")));
    }

    rs.close();
    statement.close();
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
      Node tree = m_dom.getElement("tree",m_template);

      Integer functionId = new Integer(
                              m_dom.getTextNodeValue("function_id",doc));

      Connection conn = (Connection)m_jdbcResourcePool.getResource();

      recurse(conn,tree,functionId,null);

      Node origNode = m_dom.getElement("node",m_template);
      tree.removeChild(origNode);

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
                     Logger.getLogger("uti.nextgen.eservices.GetTreeParameters");

  private DomImpl m_dom = null;
  private String m_nullSelect = null;
  private Document m_template = null;
  private String m_isNullSelect = null;
  private String m_templateName = null;
  private JDBCResourcePool m_jdbcResourcePool = null;
}
