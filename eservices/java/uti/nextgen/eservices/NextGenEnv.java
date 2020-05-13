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
import java.util.*;
import org.w3c.dom.*;
import uti.nextgen.tools.*;

/**
 * Class containing static environment and configuration details.
 *
 * @author  Sanjeev Sharma
 */

public final class NextGenEnv
{
  /**
   * Initialises configuration, JDBC and XML template resources. 
   *
   * @param  config  Path to NextGen configuration.
   *
   * @throws ConfException
   * @throws IOException
   * @throws ResourcePoolException
   */

  public static void init(String config) throws IOException, 
                                                ConfException,
                                                ResourcePoolException
  {
    if(m_count == 0)
    {
      m_count++;
      m_xmlProps = new XMLProperties(config);

      m_jdbcResourcePool = new JDBCResourcePool();
      m_jdbcResourcePool.init("jdbc",m_xmlProps);

      m_jdbcConf = new JDBCConfig(m_xmlProps);

      m_templateTable = new Hashtable();
      Element xmlTemplates = m_xmlProps.getElement("XMLTemplates");
      NodeList list = m_xmlProps.getElements(xmlTemplates,"Template");

      for(int i = 0; i < list.getLength(); i++)
      {
        Element tmplElement = (Element)list.item(i);
        m_templateTable.put(
                 m_xmlProps.getTextNodeValue(tmplElement,"name"),
                 readFile(m_xmlProps.getTextNodeValue(tmplElement,"file")));
        
      } 
    }
  }


  /**
   * Reads the file at the given location.
   *
   * @param  location  File path
   *
   * @return  String containing contents of file. 
   *
   * @throws  IOException
   */

  private static String readFile(String location) throws IOException
  {
    File file = new File(location);
    FileInputStream fis = new FileInputStream(file);
    byte data[] = new byte[(int)file.length()];

    fis.read(data);
    fis.close();

    return new String(data);
  }


  /**
   * Returns a Document instance of the named template.
   *
   * @param  name  Name of template.
   *
   * @return Document.
   */

  public static Document getTemplate(String name)
  {
    DomImpl dom = new DomImpl();
    Document doc = dom.parse((String)m_templateTable.get(name));
 
    return doc;
  }
    

  /**
   * Returns the JDBCResourcePool 
   *
   * @return  JDBCResourcePool
   */

  public static JDBCResourcePool getResourcePool()
  {
    return m_jdbcResourcePool;
  }


  /**
   * Returns NextGen configuration properties.
   *
   * @return XMLProperties
   */

  public static XMLProperties getConfig()
  {
    return m_xmlProps;
  }


  /**
   * Returns the database configuration for the NextGen classes.
   *
   * @return  JDBCConfig
   */

  public static JDBCConfig getJDBCConfig()
  {
    return m_jdbcConf;
  }


  //members
  private static int m_count = 0;
  private static JDBCConfig m_jdbcConf = null;
  private static XMLProperties m_xmlProps = null;
  private static Hashtable m_templateTable = null;
  private static JDBCResourcePool m_jdbcResourcePool = null;
}
