/*
 * Id: $Id: JDBCConfig.java,v 1.1 2003/05/27 15:01:07 hannesh Exp $
 * 
 * $RCSfile: JDBCConfig.java,v $ $Revision: 1.1 $ $Date: 2003/05/27 15:01:07 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.tools;

import java.sql.*;
import org.w3c.dom.*;

/**
 * Encapsulates the configuration of a JDBC connection.  The class provides 
 * access to all JDBC parameters specified in a XML element or set using the 
 * set methods.
 * <p>
 * The class also establishes the connection and provides an accessor method
 * to obtain the established connection. 
 * 
 * @author  Sanjeev Sharma
 */

public class JDBCConfig extends Object
{
  /**
   * Default constructor
   */
  
  public JDBCConfig()
  {
  }


  /**
   * Initializes this config object with the given XML configuration
   *
   * @param  xmlProps  XML configuration properties containing a valid
   *                   <i>JDBCConfig</i> element.
   *
   * @throws  ConfException if this object cannot be initialised.
   */
  
  public JDBCConfig(XMLProperties xmlProps) throws ConfException
  {
    try
    {
      Element jdbcElement = xmlProps.getElement("JDBCConfig");

      setDriver(xmlProps.getTextNodeValue(jdbcElement,"Driver"));
      setUserName(xmlProps.getTextNodeValue(jdbcElement,"UserName"));
      setPassword(xmlProps.getTextNodeValue(jdbcElement,"Password"));
      setConnectionString(
                  xmlProps.getTextNodeValue(jdbcElement,"ConnectionString"));

      createConnection();
    }
    catch (SQLException sqle)
    {
      throw new ConfException(sqle);
    }
    catch (ClassNotFoundException cnfe)
    {
      throw new ConfException(cnfe);
    }
  }


  /**
   * Set the the driver string for this config object.
   *
   * @param  driver  Driver class name.
   */

  public void setDriver(String driver)
  {
    m_driver = driver;
  }


  /**
   * Returns the driver string for this config object.
   *
   * @return  String containing the class name of the driver for this
   *          object.
   */

  public String getDriver()
  {
    return m_driver;
  }


  /**
   * Set the user name for this config object.
   *
   * @param  userName  User name used to establish database connection.
   */

  public void setUserName(String userName)
  {
    m_userName = userName;
  }


  /**
   * Returns the user name for this config object.
   *
   * @return  String containing user name used by this object to establish
   *          a database connection.
   */

  public String getUserName()
  {
    return m_userName;
  }


  /**
   * Set the password for this config object.
   *
   * @param  password  Password used by this object to establish a database
   *                   connection.
   */

  public void setPassword(String password)
  {
    m_password = password;
  }


  /**
   * Returns the password for this config object.
   *
   * @return  String containing the password used by this object to establish
   *          database connection.
   */

  public String getPassword()
  {
    return m_password;
  }


  /**
   * Set the connection string for this config object.
   *
   * @param  connStr  Connection string used to establish a database connection.
   *                  This is a driver specific string.
   */

  public void setConnectionString(String connStr)
  {
    m_connStr = connStr;
  }


  /**
   * Returns the connection string for this config object.
   *
   * @return  String containing the connection string used by this object to
   *          establish a database connection.
   */

  public String getConnectionString()
  {
    return m_connStr;
  }


  /**
   * Creates a connection using the configuration properties of this object.
   *
   * @throws  ClassNotFoundException if the driver class cannot be loaded.
   * @throws  SQLException if a database connection cannot be established.
   */

  public void createConnection() throws ClassNotFoundException,
                                        SQLException
  {
    Class.forName(getDriver());
    m_conn = DriverManager.getConnection(getConnectionString(),
                                         getUserName(),
                                         getPassword());
  }


  /**
   * Returns the database connection created by this config object.
   *
   * @return  Connection created by this object.
   */

  public Connection getConnection()
  {
    try
    {
      if((!connectionValid()) || (m_conn == null) || (m_conn.isClosed()))
      {
        createConnection();
      }
    }
    catch (ClassNotFoundException cnfe)
    {
      cnfe.printStackTrace();
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }

    return m_conn;
  }


  /**
   * Tests the connection to see if it is still valid.
   *
   * @return  boolean indicating the validity of the connection.
   */

  private boolean connectionValid()
  {
    try
    {
      Statement statement = m_conn.createStatement();
      statement.executeQuery("select 1");
      statement.close();
      return true;
    }
    catch (SQLException sqle)
    {
      try
      { 
        m_conn.close();
      }
      catch (SQLException sqle2)
      {
        m_conn = null;
      }
      catch (NullPointerException npe)
      {
        m_conn = null;
      }
     
      return false;
    }
    catch (NullPointerException npe)
    {
      if(m_conn == null)
      {
        return false;
      }
      else
      {
        try
        {
          m_conn.close();
        }
        catch (SQLException sqle)
        {
          m_conn = null;
        }

        return false;
      }
    } 
  }


  /** 
   * Destroys the configuration object
   */

  public void destroy()
  {
    try
    {
      m_conn.close();
    }
    catch (SQLException sqle)
    {
      sqle.printStackTrace();
    }
  }


  //members
  private String m_driver = null;
  private String m_connStr = null;
  private String m_userName = null;
  private String m_password = null;
  private Connection m_conn = null;
} 
