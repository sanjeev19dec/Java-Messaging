/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.tools;

import java.sql.*;

/**
 * ResourcePool implementation to do JDBC connection pooling.
 *
 * @author  Sanjeev Sharma
 */

public class JDBCResourcePool extends ResourcePoolImpl
{
  /**
   * Default constructor
   */

  public JDBCResourcePool()
  {
  }


  /**
   * Creates a JDBCConfig instance from the given XMLProperties and retrieves
   * the Connection instance from the config instance.
   *
   * @param  xmlProps  XML configuration properties.
   *
   * @throws  ResourcePoolException if the JDBC connection cannot be created.
   *
   * @return  Connection that was created.
   */

  protected Object createResource(XMLProperties xmlProps)
                                                   throws ResourcePoolException
  {
    try
    {
      JDBCConfig jdbcConf = new JDBCConfig(xmlProps);
      Connection conn = jdbcConf.getConnection();

      return conn;
    }
    catch (ConfException ce)
    {
      throw new ResourcePoolException(ce);
    }
  }


  /**
   * Destroys the given Connection.
   *
   * @param  resource  Connection resource to destroy.
   *
   * @throws  ResourcePoolException if the connection could not be closed.
   */

  protected void destroyResource(Object resource) throws ResourcePoolException
  {
    try
    {
      Connection conn = (Connection)resource;

      if(conn.isClosed())
      {
        return;
      }
      else
      {
        conn.close();  
        return;
      }
    }
    catch (SQLException sqle)
    {
      throw new ResourcePoolException(sqle);
    }
  }
}
