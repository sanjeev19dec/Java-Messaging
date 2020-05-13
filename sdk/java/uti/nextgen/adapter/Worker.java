/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.adapter;

import java.util.*;

/**
 * Common interface for developing worker classes to be used by the Listener 
 * class.  
 * <p>
 * Worker instances receive notification of events that are dispatched from
 * the adapter event queue.
 * <p>
 * By providing specialised implementations of this interface we do not bind 
 * the adapter core classes to a specific problem.   This interface will allow
 * developers to apply the adapter core classes in many problem scenarios.
 *
 * @author  Sanjeev Sharma
 */

public interface Worker
{
  /**
   * Worker initialisation must be performed here.
   * <p>
   * The provided hashtable contains the parameters configured in the XML
   * configuration properties in the WorkerParams element.
   *
   * @param  params  Hashtable containing parameters needed by the Worker to
   *                 successfully complete is functions.
   */

  public void init(Hashtable params);


  /**
   * Worker specific functionality must be implemented here.  
   * <p>
   * Everytime a Listener instance removes an event from the event Queue it is 
   * presented to this method for processing.  The specific Listener instance 
   * will only continue processing events after this method has completed.
   *
   * @param  event     Object containing event.
   * @param  adapter   Adapter instance that created this Worker.
   * @param  listener  String containing the name of the listener that trigger
   *                   this method call.
   */

  public void doWork(Object event, Adapter adapter, String listener);


  /**
   * Worker specific cleanup can be performed here.
   */

  public void destroy();
}
