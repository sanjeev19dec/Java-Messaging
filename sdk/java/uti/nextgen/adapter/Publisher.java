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
 * Provides functionality to publish TIBCO Rendezvous messages.
 *
 * @author  Sanjeev Sharma
 */

public class Publisher extends CommsObject
{
  /**
   * Creates a new Publisher.
   *
   * @param  xmlProps  XML configuration properties.
   *
   * @throws  TibrvException if the Publisher cannot be created.
   * @throws  ConfException if the XML configuration cannot be accessed.
   */

  public Publisher(XMLProperties xmlProps) throws TibrvException,
                                                  ConfException
  {
    super(xmlProps);
    Element publisher = xmlProps.getElement("Communications","Publisher");
    m_transport = createTransport(publisher);

    String reqTimeout = (xmlProps.getTextNodeValue(publisher,
                                                   "RequestTimeout")).trim();

    if(reqTimeout.length() == 0)
    {
      m_reqTimeout = 30;
    }
    else
    {
      m_reqTimeout = Double.parseDouble(reqTimeout);
    }
  }


  /**
   * Publish the given TibrvMsg 
   *
   * @param  msg  Message to publish.
   *
   * @throws  TibrvException if the message could not be published.
   */

  public void publish(TibrvMsg msg) throws TibrvException
  {
    msg.setSendSubject(getSubject());

    if(getType() == RELIABLE)
    {
      ((TibrvRvdTransport)m_transport).send(msg);
    }
    else if(getType() == CERTIFIED)
    {
      ((TibrvCmTransport)m_transport).send(msg);
    }
  }
    

  /**
   * Publish the given TibrvMsg as a request
   *
   * @param  msg  Message to publish.
   *
   * @return  TibrvMsg containing the reply to the request.
   *
   * @throws  TibrvException if the message could not be published.
   */

  public TibrvMsg publishRequest(TibrvMsg msg) throws TibrvException
  {
    TibrvMsg reply = null;

    if(getType() == RELIABLE)
    {
      reply = ((TibrvRvdTransport)m_transport).sendRequest(msg,m_reqTimeout);
    }
    else if(getType() == CERTIFIED)
    {
      reply = ((TibrvCmTransport)m_transport).sendRequest(msg,m_reqTimeout);
    }

    return reply;
  }


  /**
   * Publish a reply message to the given request message. 
   *
   * @param  reply    Message to publish as reply.
   * @param  request  Original request message.
   *
   * @throws  TibrvException if the message could not be published.
   */

  public void publishReply(TibrvMsg reply, 
                           TibrvMsg request) throws TibrvException
  {
    if(getType() == RELIABLE)
    {
      ((TibrvRvdTransport)m_transport).sendReply(reply,request);
    }
    else if(getType() == CERTIFIED)
    {
      ((TibrvCmTransport)m_transport).sendReply(reply,request);
    }
  }


  /**
   * Destroys the publisher
   */

  public void destroy()
  {
    m_transport.destroy();
  }


  //members
  private double m_reqTimeout = 0;
  private TibrvTransport m_transport = null;
}
