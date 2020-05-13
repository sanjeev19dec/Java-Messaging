/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.eservices;

import org.w3c.dom.*;
import uti.nextgen.tools.*;

/**
 * The Function interface provides a generic interface for writing functions to
 * extract and convert data from a relational database into XML.
 * <p>
 * The NextGenFunctions container uses this interface to manage the
 * instantiation, initialisation and retrieval of Function implementations.
 *
 * @author  Sanjeev Sharma
 */

public interface Function
{
  /**
   * Performs function specific initialisation. All resources required by the
   * function implementation must be created here.
   *
   * @param  name      The name of the function.  This is the value of the name
   *                   attribute in the API configuration file.  This is useful
   *                   for accessing function specific configuration information
   *                   in the API configuaration.
   * @param  xmlProps  XML configuration properties for the NextGen API.
   * @param  jdbcConf  JDBC configuration used to access the database.
   */

  public void init(String name, XMLProperties xmlProps, JDBCConfig jdbcConf);


  /**
   * Function specific functionality can be implemented here if the desired
   * output is a DOM Document instance.
   * <p>
   * Implement this method if further manipulation of the created XML is
   * required.
   *
   * @param  doc        DOM Document containing parameters required to
   *                    successfully execute function specific functionality.
   * @param  functions  The NextGenFunctions container this function lives in.
   *
   * @return  Document containing the result of the method execution.
   *
   * @throws  APIException if an error occures while executing.
   */

  public Document executeForDocument(Document doc,
                                     NextGenFunctions functions)
                                                    throws APIException;


  /**
   * Function specific functionality can be implemented here if the desired
   * output is a String instance containing XML data.
   *
   * @param  doc        DOM Document containing parameters required to
                        successfully
   *                    execute function specific functionality.
   * @param  functions  The NextGenFunctions container this function lives in.
   *
   * @return  String containig XML data.
   *
   * @throws  APIException if an error occurs while executing.
   */

  public String executeForString(Document doc,
                                 NextGenFunctions functions)
                                                throws APIException;

}
