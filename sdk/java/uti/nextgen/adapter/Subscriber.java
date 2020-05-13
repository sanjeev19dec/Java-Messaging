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
 * Provides functionality to subscribe to TIBCO Rendezvous messages.
 *
 * @author  Sanjeev Sharma
 */

public class Subscriber extends CommsObject
{
  /**
   * Creates a new Subscriber.
   *
   * @param  xmlProps  XML configuration properties.
   * @param  callback  Message callback used to receive messages.
   *
   * @throws  TibrvException if the Subscriber cannot be created.
   * @throws  ConfException if the XML configuration cannot be accessed.
   */

  public Subscriber(XMLProperties xmlProps,
                    TibrvMsgCallback callback) throws TibrvException,
                                                      ConfException
  {
    super(xmlProps);

    Element subscriber = xmlProps.getElement("Communications","Subscriber");

    TibrvTransport transport = createTransport(subscriber);
    TibrvQueue queue = createQueue();

    if(getType() == RELIABLE)
    {
      m_listener = new TibrvListener(queue,
                                     callback,
                                     (TibrvRvdTransport)transport,
                                     getSubject(),
                                     null);
    }
    else if(getType() == CERTIFIED)
    {
      m_listener = new TibrvCmListener(queue,
                                       callback,
                                       (TibrvCmTransport)transport,
                                       getSubject(),
                                       null);
    }
    else if(getType() == DQ)
    {
      m_listener = new TibrvCmListener(queue,
                                       callback,
                                       (TibrvCmQueueTransport)transport,
                                       getSubject(),
                                       null);
    }
  }


  /**
   * Destroys the subscriber
   */

  public void destroy()
  {
    m_listener.destroy();
  }


  //members
  private TibrvListener m_listener = null;
}
