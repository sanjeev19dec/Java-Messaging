/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.adapter;

import org.w3c.dom.*;
import com.tibco.tibrv.*;
import uti.nextgen.tools.*;

/**
 * Class that provides the base functionality for communications using TIBCO
 * Rendezvous.
 * <p>
 * The class provides transport and queue creation functionality.
 *
 * @author  Sanjeev Sharma
 */

public class CommsObject extends Object
{
  /**
   * Constructs a new object with the given XML configuration properties.
   *
   * @param  xmlProps  XML configuration properties.
   */

  public CommsObject(XMLProperties xmlProps)
  {
    m_xmlProps = xmlProps;
  }


  /**
   * Creates a transport using the given XML element as configuration.
   *
   * @param  element  Configuration XML element.
   *
   * @return  TibrvTransport object that was created.
   *
   * @throws  TibrvException if the transport cannot be created.
   * @throws  ConfException if an error occures while accessing the XML 
   *          configuration  
   */

  protected TibrvTransport createTransport(Element element) 
                                                   throws TibrvException,
                                                          ConfException
  {
    TibrvTransport transport = null;
    TibrvRvdTransport rvdTransport = null;

    rvdTransport = new TibrvRvdTransport(
                           m_xmlProps.getTextNodeValue(element,"Service"),
                           m_xmlProps.getTextNodeValue(element,"Network"),
                           m_xmlProps.getTextNodeValue(element,"Daemon"));

    m_subject = m_xmlProps.getTextNodeValue(element,"Subject");
    String type = m_xmlProps.getAttributeValue(element,"type");

    if(type.equals("rv"))
    {
      m_type = RELIABLE;
      transport = rvdTransport;
    }
    else if(type.equals("rvcm"))
    {
      m_type = CERTIFIED;
    
      String ledger = (m_xmlProps.getTextNodeValue(element,"CMLedger")).trim();
      String name = (m_xmlProps.getTextNodeValue(element,"CMName")).trim();
        
      boolean reqOld = 
         new Boolean(
           m_xmlProps.getAttributeValue(element,"CMRequestOld")).booleanValue();
      
      boolean syncLedger = 
         new Boolean(
           m_xmlProps.getAttributeValue(element,"CMSyncLedger")).booleanValue();

      if(ledger.length() == 0)
      {
        transport = new TibrvCmTransport(rvdTransport,name,reqOld);
      }
      else
      {
        transport = new TibrvCmTransport(rvdTransport,
                                         name,
                                         reqOld,
                                         ledger,
                                         syncLedger);
      }
    }
    else if(type.equals("rvcmq"))
    {
      m_type = DQ;
   
      int workerWeight = 1;
      int workerTasks = 1;
      int schedulerWeight = 1;
      double schedulerHeartbeat = 1.0;
      double schedulerActivation = 3.5;

      String name = (m_xmlProps.getTextNodeValue(element,"CMName")).trim();
      String strWorkerWeight = 
         (m_xmlProps.getTextNodeValue(element,"CMQWorkerWeight")).trim();
      String strWorkerTasks = 
         (m_xmlProps.getTextNodeValue(element,"CMQWorkerTasks")).trim();
      String strSchedWeight = 
         (m_xmlProps.getTextNodeValue(element,"CMQSchedulerWeight")).trim();
      String strSchedHb = 
         (m_xmlProps.getTextNodeValue(element,"CMQSchedulerHeartbeat")).trim();
      String strSchedAct = 
         (m_xmlProps.getTextNodeValue(element,"CMQSchedulerActivation")).trim();

      if(strWorkerWeight.length() != 0)
      {
        workerWeight = Integer.parseInt(strWorkerWeight);
      }

      if(strWorkerTasks.length() != 0)
      {
        workerTasks = Integer.parseInt(strWorkerTasks);
      }

      if(strSchedWeight.length() != 0)
      {
        schedulerWeight = Integer.parseInt(strSchedWeight);
      }

      if(strSchedHb.length() != 0)
      {
        schedulerHeartbeat = Double.parseDouble(strSchedHb);
      }

      if(strSchedAct.length() != 0)
      {
        schedulerActivation = Double.parseDouble(strSchedAct);
      }

      transport = new TibrvCmQueueTransport(rvdTransport,
                                            name,
                                            workerWeight,
                                            workerTasks,
                                            schedulerWeight,
                                            schedulerHeartbeat,
                                            schedulerActivation);
    }
                                            
    return transport;                      
  }


  /**
   * Creates a TibrvQueue and returns it.  A dispatcher is also started for the
   * created queue.
   *
   * @return  TibrvQueue for message dispatching
   *
   * @throws  TibrvException if the queue cannot be created.
   */

  protected TibrvQueue createQueue() throws TibrvException
  {
    TibrvQueue queue = new TibrvQueue();
    TibrvDispatcher dispatcher = new TibrvDispatcher(queue);

    return queue;
  }    


  /**
   * Returns the type of this object.
   *
   * @return  int inidicating the type of this object.
   */

  public int getType()
  {
    return m_type;
  }


  /**
   * Returns the subject used by this object.
   *
   * @return String containing the subject used by this object. 
   */

  public String getSubject()
  {
    return m_subject;
  }


  /**
   * Field indicating that this object is enabled for reliable delivery.
   */

  public static final int RELIABLE = 1;


  /**
   * Field indicating that this object is enabled for certified delivery.
   */

  public static final int CERTIFIED = 2;


  /**
   * Field indicating that this object forms part of a Distributed Queue.
   */

  public static final int DQ = 3;


  /**
   * XML configuration properties.
   */

  protected XMLProperties m_xmlProps = null;


  //members
  private int m_type = 0;
  private String m_subject = null;
}
