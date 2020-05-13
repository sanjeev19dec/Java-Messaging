/*
 * Id: $Id: ResourcePoolException.java,v 1.1 2003/05/27 15:01:07 hannesh Exp $
 * 
 * $RCSfile: ResourcePoolException.java,v $ $Revision: 1.1 $ $Date: 2003/05/27 15:01:07 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.tools;

/**
 * Exception that is generated when  exceptions occurs during ResourcePool 
 * operations.
 *
 * @author  Hannes
 */

public class ResourcePoolException extends Exception
{
  /**
   * Creates a new ResourcePoolException from the given Exception.
   *
   * @param  ex  Underlying exception that occured.
   */

  public ResourcePoolException(Exception ex)
  {
    super(ex);
  }


  /**
   * Generates a ResourcePoolException with the given message.
   *
   * @param  msg  String containing the exception message.
   */

  public ResourcePoolException(String msg)
  {
    super(msg);
  }
}
