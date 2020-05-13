/*
 * Id: $Id: SOAPClientException.java,v 1.1 2003/05/27 15:01:07 hannesh Exp $
 * 
 * $RCSfile: SOAPClientException.java,v $ $Revision: 1.1 $ $Date: 2003/05/27 15:01:07 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.soapclient;

/**
 * Exception that is generated when  exceptions occurs during SOAPClient 
 * operations.
 *
 * @author  Hannes
 */

public class SOAPClientException extends Exception
{
  /**
   * Creates a new SOAPClientException from the given Exception.
   *
   * @param  ex  Underlying exception that occured.
   */

  public SOAPClientException(Exception ex)
  {
    super(ex);
  }


  /**
   * Generates a SOAPClientException with the given message.
   *
   * @param  msg  String containing the exception message.
   */

  public SOAPClientException(String msg)
  {
    super(msg);
  }
}
