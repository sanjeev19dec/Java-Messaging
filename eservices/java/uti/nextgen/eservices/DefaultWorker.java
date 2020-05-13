/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.eservices;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import com.tibco.tibrv.*;
import java.util.logging.*;
import uti.nextgen.tools.*;
import uti.nextgen.adapter.*;

/**
 * Default worker implementation for UTI NextGen API.
 * <p>
 * A Worker instance receives notification of events that are dispatched from
 * the adapter event queue.  The specific instance that received the
 * notification extracts data from the received event (Event objects are
 * TibrvMsg instances).  The extracted data is used to decide which NextGen
 * Function instance should be invoked to process the received event.
 * <p>
 * The output of the invoked NextGen Function instance is wrapped in a TIBCO
 * Rendezvous message and published to its final destination.
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
   * <li> APIConfig - Contains the file name of the API configuration file.</li>
   * <li> Mode - Specifies the messaging mode of the worker: "publish | reply".
   * </li>
   *
   * @param  params  Hashtable containing parameters needed by the Worker to
   *                 successfully complete is functions.
   */

  public void init(Hashtable params)
  {
    m_dom = new DomImpl();
    m_mode = ((String)params.get("Mode")).toLowerCase();

    try
    {
      NextGenEnv.init((String)params.get("APIConfig"));
      m_nextGenFunctions = new NextGenFunctions();
    }
    catch (IOException ioe)
    {
      m_logger.log(Level.SEVERE,"Unable to initialise NextGenEnv",ioe);
    }
    catch (ConfException ce)
    {
      m_logger.log(Level.SEVERE,"Unable to initialise NextGenEnv",ce);
    }
    catch (ResourcePoolException rpe)
    {
      m_logger.log(Level.SEVERE,"Unable to initialise JDBCResourcePool",rpe);
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
      Document doc = m_dom.parse(xmlStr);

      String requestType =
                     (m_dom.getTextNodeValue("req_type",doc)).toLowerCase();

      m_logger.log(Level.INFO,"Received message of request type: "+requestType);

      Function function =
                m_nextGenFunctions.getFunctionByRequestType(requestType);

      String outXmlStr = function.executeForString(doc,m_nextGenFunctions);

      TibrvMsg outMsg = new TibrvMsg();
      outMsg.add("DATA",outXmlStr);

      if(m_mode.equals("publish"))
      {
        adapter.getPublisher().publish(outMsg);
        m_logger.log(Level.FINE,"Published message: "+outMsg);
      }
      else if(m_mode.equals("reply"))
      {
        adapter.getPublisher().publishReply(outMsg,(TibrvMsg)event);
        m_logger.log(Level.FINE,"Replied with message: "+outMsg);
      }
    }
    catch (TibrvException te)
    {
      m_logger.log(Level.WARNING,
                   "Encountered TIBCO Rendezvous exception",
                   te);
    }
    catch (Exception e)
    {
      m_logger.log(Level.WARNING,"Encountered general exception",e);
    }
  }


  /**
   * Worker specific cleanup can be performed here.
   * <p>
   * Destroys the NextGenFunctions container.
   */

  public void destroy()
  {
    m_nextGenFunctions.destroy();
  }


  //members
  private static Logger m_logger =
                    Logger.getLogger("uti.nextgen.eservices.DefaultWorker");

  private String m_mode = null;
  private DomImpl m_dom = null;
  private NextGenFunctions m_nextGenFunctions = null;
}
