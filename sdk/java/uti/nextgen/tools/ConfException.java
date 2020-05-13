/*
 * Id: $Id: ConfException.java,v 1.1 2003/05/27 15:01:07 hannesh Exp $
 * 
 * $RCSfile: ConfException.java,v $ $Revision: 1.1 $ $Date: 2003/05/27 15:01:07 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.tools;

/**
 * Exception that is generated when a exceptions occur accessing XMLProperties
 * objects
 *
 * @author  Hannes
 */

public class ConfException extends Exception
{
  /**
   * Creates a new ConfException from the given Exception.
   *
   * @param  ex  Underlying exception that occured.
   */

  public ConfException(Exception ex)
  {
    super(ex);
  }


  /**
   * Generates a ConfException with the given message.
   *
   * @param  msg  String containing the exception message.
   */

  public ConfException(String msg)
  {
    super(msg);
  }
}
