/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.soapclient;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.soap.*;
import uti.nextgen.tools.*;

/**
 * Generic SOAP client to send DOM document instances to configured end points.
 *
 * @author  Sanjeev Sharma
 */

public class SOAPClient extends Object
{
  /**
   * Creates a SOAP client by creating all required SOAP factories and 
   * intialising all the supported end point configurations.
   *
   * @param  xmlProps  XML configuration properties.
   *
   * @throws  SOAPClientException if SOAP factories cannot be created.
   * @throws  ConfException if end point configuration cannot be created.
   */

  public SOAPClient(XMLProperties xmlProps) throws SOAPClientException,
                                                   ConfException
  {
    try
    {
      m_soapConnFactory = SOAPConnectionFactory.newInstance();
      m_soapFactory = SOAPFactory.newInstance();
      m_msgFactory = MessageFactory.newInstance();
    }
    catch (SOAPException spe)
    {
      throw new SOAPClientException(spe);
    }

    m_endPoints = new Hashtable();

    Element client = xmlProps.getElement("SOAPClient");
    Element endPoints = xmlProps.getElement(client,"EndPoints");
    NodeList list = xmlProps.getElements(endPoints,"EndPoint");

    for(int i = 0; i < list.getLength(); i++)
    {
      Element element = (Element)list.item(i);

      m_endPoints.put(xmlProps.getTextNodeValue(element,"Name"),
                      xmlProps.getTextNodeValue(element,"URL"));
    }
  }


  /**
   * Calls the named end point and waits for a response.
   *
   * @param  name  Name of end point to call.  This must be one of the names 
   *               contained in the SOAPClient configuration.
   * @param  doc   Document to send to end point.
   * 
   * @return  Element containing the root element of the response.
   *
   * @throws  SOAPClientExcpetion if any of the call operations fail.
   */

  public Element call(String name, Document doc) throws SOAPClientException
  {
    try
    {
      SOAPConnection connection = m_soapConnFactory.createConnection();
      SOAPMessage message = m_msgFactory.createMessage();
      SOAPHeader header = message.getSOAPHeader();
      SOAPBody body = message.getSOAPBody();

      header.detachNode();
      body.addDocument(doc);

      URL endPoint = new URL((String)m_endPoints.get(name));

      SOAPMessage response = connection.call(message,endPoint); 
      SOAPBody respBody = response.getSOAPBody();

      Iterator iter = respBody.getChildElements();

      Element respElement = (Element)iter.next();

      connection.close(); 
      message = null;
      response = null;

      return respElement;
    }
    catch (SOAPException spe)
    {
      throw new SOAPClientException(spe);
    }
    catch (MalformedURLException mfe)
    {
      throw new SOAPClientException(mfe);
    }
  }


  //members
  private Hashtable m_endPoints = null;
  private SOAPFactory m_soapFactory = null;
  private MessageFactory m_msgFactory = null;
  private SOAPConnectionFactory m_soapConnFactory = null; 
}
