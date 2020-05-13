/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.adapter;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import com.tibco.tibrv.*;
import java.util.logging.*;
import uti.nextgen.tools.*;

/**
 * Generic adapter class.
 * <p>
 * This class provides a configurable application component to provide 
 * communication between stand-alone applications and TIBCO integration products
 * using TIBCO Rendezvous.
 * <p>
 * The Adapter class implements the TibrvFtMemberCallback.  This means that
 * the Adapter can be configured to be part of a fault tolerant group.
 * <p>
 * The Adapter is driven by an XML configuration file that has to major parts:
 * <p>
 * <li> Communications:  Communications refer to all TIBCO Rendezvous 
 *      communication parameters.</li>
 * <li> Queueing:  Queueing refers to the internal queueing parameters for 
 *      incoming messages.  It also defines the class that must be used to 
 *      process incoming messages.</li>
 *      
 * @author  Sanjeev Sharma
 */

public class Adapter extends Object implements TibrvFtMemberCallback,
                                               TibrvMsgCallback
{
  /**
   * Constructs a new adapter.
   *
   * @param  xmlFile  Location of XML configuration file.
   */

  public Adapter(String xmlFile)
  {
    try
    {
      Tibrv.open(Tibrv.IMPL_NATIVE);

      m_xmlProps = new XMLProperties(xmlFile);

      Element ftElement = m_xmlProps.getElement("Communications",
                                              "FaultTolerance"); 

      boolean enableFt = new Boolean(
              m_xmlProps.getAttributeValue(ftElement,"enable")).booleanValue();

      if(enableFt)
      {
        m_logger.log(Level.INFO,"Fault tolerance enabled");
        initFt(ftElement);
      }
      else
      {
        m_logger.log(Level.INFO,"Fault tolerance disabled");
        start();
      }
    }
    catch (IOException ioe)
    {
      m_logger.log(Level.SEVERE,"Unalbe to open XML config: "+ioe.getMessage(),
                   ioe);
    }
    catch (ConfException ce)
    {
      m_logger.log(Level.SEVERE,"Unable to access XML config: "+ce.getMessage(),
                   ce);
    }
    catch (TibrvException te)
    {
      m_logger.log(Level.SEVERE,"TIBCO Rendezvous exception: "+te.getMessage(),
                   te);
    }
  }     
  

  /**
   * Creates and starts the Fault tolerant member.
   *
   * @param  ftElement  XML element containing fault tolerance configuration.
   *
   * @throws  TibrvException
   * @throws  ConfException
   */

  private void initFt(Element ftElement) throws TibrvException,
                                                ConfException
  {
    TibrvQueue queue = new TibrvQueue();

    TibrvRvdTransport transport = new TibrvRvdTransport(
                               m_xmlProps.getTextNodeValue(ftElement,"Service"),
                               m_xmlProps.getTextNodeValue(ftElement,"Network"),
                               m_xmlProps.getTextNodeValue(ftElement,"Daemon"));

    int weight = 1;
    int activeGoal = 1;
    double heartbeat = 1.5;
    double preparation = 0.0;
    double activation = 3.0;

    String groupName = 
        (m_xmlProps.getTextNodeValue(ftElement,"GroupName")).trim();
    String strWeight = 
        (m_xmlProps.getTextNodeValue(ftElement,"Weight")).trim();
    String strActiveGoal = 
        (m_xmlProps.getTextNodeValue(ftElement,"ActiveGoal")).trim();
    String strHb = 
        (m_xmlProps.getTextNodeValue(ftElement,"Heartbeat")).trim();
    String strPrep = 
        (m_xmlProps.getTextNodeValue(ftElement,"Preparation")).trim();
    String strAct = 
        (m_xmlProps.getTextNodeValue(ftElement,"Activation")).trim();

    if(strWeight.length() != 0)
    {
      weight = Integer.parseInt(strWeight);
    }
    
    if(strActiveGoal.length() != 0)
    {
      activeGoal = Integer.parseInt(strActiveGoal);
    }

    if(strHb.length() != 0)
    {
      heartbeat = Double.parseDouble(strHb);
    }

    if(strPrep.length() != 0)
    {
      preparation = Double.parseDouble(strPrep);
    }

    if(strAct.length() != 0)
    {
      activation = Double.parseDouble(strAct);
    }


    TibrvFtMember ftMember = new TibrvFtMember(queue,
                                               this,
                                               transport,
                                               groupName,
                                               weight,
                                               activeGoal,
                                               heartbeat,
                                               preparation,
                                               activation,
                                               null);

    TibrvDispatcher dispatcher = new TibrvDispatcher(queue);

    m_logger.log(Level.CONFIG,"Fault tolerant member created with config: \n"+
                              "   Group:                "+groupName+"\n"+
                              "   Weight:               "+weight+"\n"+
                              "   Active goal:          "+activeGoal+"\n"+
                              "   Heartbeat:            "+heartbeat+"\n"+
                              "   Preparation interval: "+preparation+"\n"+
                              "   Activation interval:  "+activation);
  }


  /**
   * Initialises the event queue and all the Listener Threads.
   *
   * @param  element  XML configuration element.
   *
   * @throws  ConfException
   */

  private void initQueueing(Element element) throws ConfException
  { 
    m_listenerVector = new Vector();
    m_listenerGroup = new ThreadGroup("Adapter");

    int count = Integer.parseInt(m_xmlProps.getTextNodeValue(element,
                                                             "Count"));

    int queueDepth = Integer.parseInt(m_xmlProps.getTextNodeValue(element,
                                                                 "QueueDepth"));
    m_queue = new Queue(queueDepth);

    for(int i = 0; i < count; i++)
    {
      Listener listener = new Listener(m_queue,
                                       m_listenerGroup,
                                       i,
                                       m_xmlProps,
                                       this);
      listener.start();
      m_listenerVector.add(listener);
    }

    m_logger.log(Level.CONFIG,"Queueing initialised with config: \n"+
                              "  Queue depth:    "+queueDepth+"\n"+
                              "  Listener count: "+count);
  }


  /**
   * Receives notification of Fault tolerant events.
   *
   * @param   invoker  Fault tolerant member that invoked this method.
   * @param   ftgroup  The fault tolerant group to which this member belongs.
   * @param   action   The fault tolerant action that was invoked.
   */

  public void onFtAction(TibrvFtMember invoker,String ftgroup,int action)
  {
    switch(action)
    {
      case TibrvFtMember.ACTIVATE:    start();break;
      case TibrvFtMember.DEACTIVATE:  stop();break;
    }
  }


  /**
   * Receives notification of messages and places them in the event queue
   * for processing.
   *
   * @param  invoker  The listener that invoked this method.
   * @param  msg      The received message.
   */

  public void onMsg(TibrvListener invoker, TibrvMsg msg)
  {
    try
    {
      m_queue.push(msg);
      m_logger.log(Level.FINE,"Enqueued message: "+msg);
    }
    catch (QueueDiscardWarning qdw)
    {
      m_logger.log(Level.WARNING,"Max queue depth reached: "+qdw.getMessage(),
                   qdw);
    }
  }


  /**
   * Starts the Adapter by creating all members specified in the XML 
   * configuration.
   */

  public void start()
  {
    try
    {
      Element listenerElement = m_xmlProps.getElement("Queueing","Listener");

      initQueueing(listenerElement);
      m_logger.log(Level.INFO,"Queueing initialised");
 
      Element subElement = m_xmlProps.getElement("Communications","Subscriber");
      Element pubElement = m_xmlProps.getElement("Communications","Publisher");

      boolean initPub = new Boolean(
              m_xmlProps.getAttributeValue(pubElement,"enable")).booleanValue();

      boolean initSub = new Boolean(
              m_xmlProps.getAttributeValue(subElement,"enable")).booleanValue();

      if(initPub)
      {
        m_publisher = new Publisher(m_xmlProps);      
        m_logger.log(Level.INFO,"Publisher initialised");
      }

      if(initSub)
      {
        m_subscriber = new Subscriber(m_xmlProps,this);
        m_logger.log(Level.INFO,"Subscriber initialised");
      }

      m_logger.log(Level.INFO,"Communications initialised");
    }
    catch (ConfException ce)
    {
      m_logger.log(Level.SEVERE,"Config exception during startup: "+
                   ce.getMessage(),ce);
    }
    catch (TibrvException te)
    {
      m_logger.log(Level.SEVERE,"TIBCO Rendezvous exception during startup: "+
                   te.getMessage(),te);
    }
  }


  /**
   * Stops the adapter by destroying all members.
   */

  public void stop()
  {
    m_subscriber.destroy();
    m_publisher.destroy();

    m_subscriber = null;
    m_publisher = null;

    m_logger.log(Level.INFO,"Communications stopped");

    for(int i = 0; i < m_listenerVector.size(); i++)
    {
      Listener listener = (Listener)m_listenerVector.elementAt(i);
      listener.stopListener();
      listener.destroy();

      listener = null;
    }

    m_listenerVector = null;
    m_listenerGroup.destroy();

    m_logger.log(Level.INFO,"Queueing stopped");
  }


  /**
   * Returns the member Publisher used by this adapter.
   *
   * @return  Publisher used by this adapter.
   */

  public Publisher getPublisher()
  {
    return m_publisher;
  }


  /**
   * Application starting point.
   *
   * @param  args  Command line arguments
   */

  public static void main(String args[])
  {
    Adapter adapter = new Adapter(args[0]);
  }


  //member
  private static Logger m_logger = 
                    Logger.getLogger("uti.nextgen.adapter.Adapter");

  private Queue m_queue = null;
  private Publisher m_publisher = null; 
  private Subscriber m_subscriber = null;
  private Vector m_listenerVector = null;
  private XMLProperties m_xmlProps = null;
  private ThreadGroup m_listenerGroup = null;
}
