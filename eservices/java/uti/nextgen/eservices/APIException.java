/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.eservices;

/**
 * Exception that is generated when exceptions occur while executing Function
 * interface methods
 *
 * @author  Sanjeev Sharma
 */

public class APIException extends Exception
{
  /**
   * Creates a new APIException from the given Exception.
   *
   * @param  ex  Underlying exception that occured.
   */

  public APIException(Exception ex)
  {
    super(ex);
  }


  /**
   * Generates a APIException with the given message.
   *
   * @param  msg  String containing the exception message.
   */

  public APIException(String msg)
  {
    super(msg);
  }
}
