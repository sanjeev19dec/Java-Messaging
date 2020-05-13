/*
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.adapter; 

import java.util.*;
import org.w3c.dom.*;
import java.util.logging.*;
import uti.nextgen.tools.*;

/**
 * The listener class provides a mechanism for dispatching events from the 
 * Queue class and a common way of handling events dispatched from the 
 * Queue class.
 * 
 * @author  Sanjeev Sharma
 */

public class Listener extends Thread
{
  /**
   * Constructs a new Listener.
   *
   * @param  queue      Event queue.
   * @param  thrdGrp    ThreadGroup to add listener to. Applications that extend
   *                    the listener class must provide a thread group to add 
   *                    the listener to.
   * @param  instance   Listener instance count used to name listeners.  Must
   *                    provided by the calling class.
   * @param   xmlProps  XML configuration properties.
   * @param   adapter   Adapter instance that created this listener.
   */

  public Listener(Queue queue,
                  ThreadGroup thrdGrp,
                  int instance,
                  XMLProperties xmlProps,
                  Adapter adapter)
  {
    super(thrdGrp,thrdGrp.getName()+"-listener-"+instance);
    m_queue = queue;
    m_adapter = adapter;

    try
    {
      Element listenerConf = xmlProps.getElement("Queueing","Listener");

      String workerClass = xmlProps.getTextNodeValue(listenerConf,
                                                     "WorkerClass");

      Element workerParams = xmlProps.getElement(listenerConf,"WorkerParams");
      NodeList paramList = workerParams.getChildNodes();

      Hashtable params = new Hashtable();

      StringBuffer buf = new StringBuffer();
      buf.append("Worker parameters: \n");

      for(int i = 0; i < paramList.getLength(); i++)
      {
        Node node = paramList.item(i);

        if(!(node.getNodeName().equals("#text")))
        {
          params.put(node.getNodeName(),xmlProps.getTextNodeValue(node));
          buf.append("    "+node.getNodeName()+"   "+
                     xmlProps.getTextNodeValue(node)+"\n"); 
        }
      }
      
      m_worker = (Worker)((Class.forName(workerClass)).newInstance());
      m_worker.init(params);
   
      m_logger.log(Level.CONFIG,
                   "Instantiated Worker implementation: "+workerClass);
      m_logger.log(Level.CONFIG,buf.toString());

      m_logger.log(Level.INFO,"Initialised Listener instance: "+getName());

      buf = null;
    }
    catch (ConfException ce)
    {
      m_logger.log(Level.SEVERE,
                   "Configuration exception in Listener instance: "+getName()+
                   ": "+ce.getMessage(),ce);
    }
    catch (ClassNotFoundException cnfe)
    {
      m_logger.log(Level.SEVERE,
                   "Worker class not found for Listener intance: "+getName(),
                   cnfe);
    }
    catch (InstantiationException ie)
    {
      m_logger.log(Level.SEVERE,
                   "Worker instantiation failed for Listener instance: "+
                   getName(),
                   ie);
    }
    catch (IllegalAccessException ae)
    {
      m_logger.log(Level.SEVERE,
                   "Worker access exception in Listener instance: "+getName(),
                   ae);
    }
  }


  /**
   * Dispatches events from the event queue and presents them to the member
   * Worker.
   */

  public void run()
  {
    Object data = null;
    
    while(true)
    {
      try
      {
        data = m_queue.pop();

        if(data instanceof String)
        {
          m_logger.log(Level.INFO,
                       "Flushing queue in Listener instance: "+getName());

          if(!(((String)data).equals("_flush_")))
          {
            m_worker.doWork(data,m_adapter,getName());
          }
          else
          {
            break;
          }
        }
        else
        {
          m_worker.doWork(data,m_adapter,getName());
        }
      }
      catch (InterruptedException ie)
      {
        ie.printStackTrace();
      }
    }
  }


  /**
   * Stops the listener by flushing the event queue.
   */

  public void stopListener()
  {
    try
    {
      m_queue.push("_flush_");
    }
    catch (QueueDiscardWarning qdw)
    {
      m_logger.log(Level.WARNING,
                   "Unable to flush queue for Listener instance: "+getName(),
                   qdw);
    }
  }    
    

 /**
  * Destroys the Listener by destroying the member worker.
  */

 public void destroy()
 {
   m_worker.destroy();
 }


  //members
  private static Logger m_logger = 
                        Logger.getLogger("uti.nextgen.adapter.Listener");

  private Queue m_queue = null;
  private Worker m_worker = null;
  private Adapter m_adapter = null;
}
