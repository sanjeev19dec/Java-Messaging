/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.ds;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import javax.naming.*;
import com.tibco.tibrv.*;
import uti.nextgen.tools.*;
import uti.nextgen.adapter.*;
import java.util.logging.*;

/**
 * Default worker implementation for NextGen Directory Services. 
 * <p>
 * A Worker instance receives notification of events that are dispatched from 
 * the adapter event queue.  The specific instance that received the 
 * notification extracts data from the received event (Event objects are 
 * TibrvMsg instances).  The extracted data is used to invoke a Directory 
 * Service operation.  
 *
 * @author  Sanjeev Sharma
 */

public class DefaultWorker implements Worker
{
  /**
   * Worker initialisation must be performed here.
   * <p>
   * Initialises this Worker with the given configuration properties.
   * <p>
   * The given hashtable contains the following values:
   * <p>
   * <li> DSConfig - Contains the file name of the DS configuration file.</li>
   * <li> Mode - Specifies the messaging mode of the worker: "publish | reply".
   * </li>
   *
   * @param  params  Hashtable containing parameters needed by the Worker to
   *                 successfully complete is functions.
   */

  public void init(Hashtable params)
  {
    m_mode = ((String)params.get("Mode")).toLowerCase();

    try
    {
      m_dsOperation = new DsOperation((String)params.get("DSConfig"));
    }
    catch (IOException ioe)
    {
      m_logger.log(Level.SEVERE,"Unable to initialise DsOperation: "+
                   ioe.getMessage(),ioe);
    }
    catch (ConfException ce)
    {
      m_logger.log(Level.SEVERE,"Unable to initialise DsOperation: "+
                   ce.getMessage(),ce);
    }
    catch (NamingException ne)
    {
      m_logger.log(Level.SEVERE,"Unable to initialise DsOperation: "+
                   ne.getMessage(),ne);
    }
  }


  /**
   * Worker specific functionality must be implemented here.  
   * <p>
   * Does a lookup for the correct function to execute. If the function was
   * executed successfully an outgoing XML string is published.
   *
   * @param  event     Object containing event.
   * @param  adapter   Adapter instance that created this Worker.
   * @param  listener  String containing the name of the listener that triggered
   *                   this method call.
   */

  public void doWork(Object event, Adapter adapter, String listener)
  {
    try
    {
      String xmlStr = (String)(((TibrvMsg)event).get("DATA"));
      m_logger.log(Level.FINE,"Worker for listener, "+listener+
                   ", accepted message: "+xmlStr);

      String outXmlStr = m_dsOperation.doOperation(xmlStr);
    
      TibrvMsg outMsg = new TibrvMsg();
      outMsg.add("DATA",outXmlStr);

      if(m_mode.equals("publish"))
      { 
        adapter.getPublisher().publish(outMsg);
        m_logger.log(Level.FINE,"Worker for listener, "+listener+
                     " ,published message: "+outMsg);
      }
      else if(m_mode.equals("reply"))
      {
        adapter.getPublisher().publishReply(outMsg,(TibrvMsg)event);
        m_logger.log(Level.FINE,"Worker for listener, "+listener+
                     " ,replied with message: "+outMsg);
      }
    }
    catch (TibrvException te)
    {
      m_logger.log(Level.WARNING,
                   "Worker for listener, "+listener+
                   ", encountered TIBCO Rendezvous exception: "+te.getMessage(),
                   te);
    }
    catch (Exception e)
    {
      m_logger.log(Level.WARNING,"Worker for listener, "+listener+
                   ", Encountered general exception: "+e.getMessage(),e);
    }
  }


  /**
   * Worker specific cleanup can be performed here.
   * <p>
   * Destroys the NextGenFunctions container.
   */

  public void destroy()
  {
  }


  //members
  private static Logger m_logger = 
                    Logger.getLogger("uti.nextgen.ds.DefaultWorker");

  private String m_mode = null;
  private DsOperation m_dsOperation = null;
}
