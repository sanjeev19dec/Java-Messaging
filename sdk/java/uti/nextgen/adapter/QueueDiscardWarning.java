/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.adapter; 

/**
 * Warning to indicate that an event queue has reached maximum depth and
 * is discarding new events until the queue depth reaches a point below the
 * maximum depth.
 *
 * @author  Sanjeev Sharma
 */

public class QueueDiscardWarning extends Exception
{
  /**
   * Constructor that calls the parent class Exception.
   *
   * @param  info  Warning description
   */

  public QueueDiscardWarning(String info)
  {
    super(info);
  }
}
