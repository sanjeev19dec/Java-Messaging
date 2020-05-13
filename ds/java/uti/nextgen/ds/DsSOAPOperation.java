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
import uti.nextgen.tools.*;
import uti.nextgen.soapclient.*;

/**
 * Client interface for web services that support dsoparation instance 
 * documents.  
 * <p> 
 * This class handles all interaction with the web service being invoked. 
 * DsMessage instances are converted into XML documents for SOAP requests and
 * SOAP reply documents are converted into DsMessage instances.
 *
 * @author  Sanjeev Sharma
 */

public class DsSOAPOperation extends Object
{
  /**
   * Initialises this operation with the given XML configuration properties.
   * Initialises the template XML document and the SOAPClient.
   *
   * @param  xmlProps  XML configuration properties.
   *
   * @throws  SOAPClientException if the the SOAPCLient cannot be created.
   * @throws  IOException if the template document could not be read.
   * @throws  ConfException if an exception occurs while accessing the
   *          configuration properties.
   */

  public DsSOAPOperation(XMLProperties xmlProps) throws SOAPClientException,
                                                        ConfException,
                                                        IOException
  {
    m_soapClient = new SOAPClient(xmlProps); 

    Element templateElement = xmlProps.getElement("Template");
    Element defEndPointElement = xmlProps.getElement("DefaultEndPoint");

    m_defaultEndPoint = xmlProps.getTextNodeValue(defEndPointElement);

    File file = new File(xmlProps.getTextNodeValue(templateElement));

    FileInputStream fis = new FileInputStream(file);
    byte data[] = new byte[(int)file.length()];

    fis.read(data);
    fis.close();

    m_templateStr = new String(data); 
    m_parser = new DOMParser();
  }  
 

  /**
   * Initialise this operation with the name of the XML configuration.
   *
   * @param  xmlConf  Name of the XML configuration file.
   * 
   * @throws  SOAPClientException if the the SOAPCLient cannot be created.
   * @throws  IOException if the template document could not be read.
   * @throws  ConfException if an exception occurs while accessing the 
   *          configuration properties.
   */

  public DsSOAPOperation(String xmlConf) throws SOAPClientException,
                                                ConfException,
                                                IOException
  {
    this(new XMLProperties(xmlConf));
  }


  /**
   * Removes the named element from the given document.
   *
   * @param  name  Element to remove.
   * @param  doc   Document containinig element.
   */

  private void removeElement(String name, Document doc)
  {
    Element element = m_parser.getElement(name,doc);
    doc.getDocumentElement().removeChild(element);
  }


  /**
   * Creates a dsoparation Document instance.
   *
   * @return  Document.
   */

  private Document getDocument()
  {
    return m_parser.parse(m_templateStr);
  }


  /**
   * Construct a DOM document from the given DsMessage.
   *
   * @param  dsMsg  DsMessage.
   *
   * @return  Document
   */

  private Document createDocument(DsMessage dsMsg)
  {
    Document doc = getDocument();

    String objType = dsMsg.getObjectType();

    if(objType.equals("person"))
    {
      removeElement("group",doc); 
      removeElement("error",doc); 
    }
    else if(objType.equals("group"))
    {
      removeElement("person",doc); 
      removeElement("error",doc); 
    }
    else
    {
      removeElement("person",doc); 
      removeElement("group",doc); 
    }

    Element credentials = m_parser.getElement("credentials",doc);
    Element binddn = m_parser.getElement("binddn",credentials);
    binddn.appendChild(doc.createTextNode(m_binddn));
    
    Element passwd = m_parser.getElement("password",credentials);
    passwd.appendChild(doc.createTextNode(m_passwd));

    Element action = m_parser.getElement("action",doc);
    m_parser.setNodeValue("type",dsMsg.getType(),action);
    m_parser.setNodeValue("subtype",dsMsg.getSubtype(),action);
 
    LinkedList retAttrNames = dsMsg.getReturnAttributeNames();

    if(retAttrNames.size() > 0)
    {
      Element returnElement = doc.createElement("return");
      action.appendChild(returnElement);

      for(int i = 0; i < retAttrNames.size(); i++)
      {
        Element attrElement = doc.createElement("attribute");
        attrElement.appendChild(
                            doc.createTextNode((String)retAttrNames.get(i)));

        returnElement.appendChild(attrElement);
      }
    }

    Element container = m_parser.getElement(objType,doc);

    String attrNames[] = dsMsg.getAttributeNames();

    for(int i = 0; i < attrNames.length; i++)
    {
      String attrName = attrNames[i]; 

      Object value = dsMsg.getAttributeValue(attrName);

      if(value == null)
      {
        continue;
      }
      else
      {
        if(value instanceof String)
        {
          Element element = doc.createElement(attrName);
          container.appendChild(element);
          element.appendChild(doc.createTextNode((String)value));
        }
        else if(value instanceof Vector)
        {
          Vector vector = (Vector)value;

          for(int k = 0; k < vector.size(); k++)
          { 
            Element element = doc.createElement(attrName);     
            container.appendChild(element);
            element.appendChild(
                            doc.createTextNode((String)vector.elementAt(k)));
          }
        }
      }
    }

    return doc;
  }


  /**
   * Add all elements of the given NodeList into the given array starting at
   * the given offset.
   *
   * @param  list     NodeList containing information.
   * @param  arr      Array to populate.
   * @param  offset   Start at this offset.
   * @param  type     DsMessage type
   * @param  subtype  DsMessage subtype
   * @param  objType  DsMessage object type
   */

  private void addToArray(NodeList list, 
                          DsMessage arr[], 
                          int offset,
                          String type,
                          String subtype,
                          String objType)
  {
    for(int i = 0; i < list.getLength(); i++)
    {
      Node node = list.item(i);
      NodeList detailList = node.getChildNodes();
      
      DsMessage msg = new DsMessage();
      msg.setType(type);
      msg.setSubtype(subtype);
      msg.setObjectType(objType);

      msg.addAttribute("dn",m_parser.getAttrValue(node,"dn"));

      for(int k = 0; k < detailList.getLength(); k++)
      {
        Node detailNode = detailList.item(k);
        String name = detailNode.getNodeName();

        NodeList mvList = m_parser.getElements(name,(Element)node);

        if(mvList.getLength() == 1)
        {
          msg.addAttribute(name,m_parser.getTextNodeValue(detailNode));
        }
        else
        {
          Vector vector = new Vector();

          for(int j = 0; j < mvList.getLength(); j++)
          {
            vector.add(m_parser.getTextNodeValue(mvList.item(j)));
          }

          msg.addAttribute(name,vector);
        }
      } 

      arr[offset + i] = msg;
    }
  }


  /**
   * Creates DsMessage instances from the given element.
   *
   * @param  element  Element containing information.
   *
   * @return  DsMessage[]
   */

  private DsMessage[] createDsMessage(Element element)
  {
    Element action = m_parser.getElement("action",element);
    String type = m_parser.getAttrValue(action,"type");
    String subtype = m_parser.getAttrValue(action,"subtype");

    Element container = m_parser.getElement("error",element);

    if(container != null)
    {
      DsMessage msg = new DsMessage();
      msg.setType(type);
      msg.setSubtype(subtype);
      msg.setObjectType("error");

      msg.addAttribute("error",m_parser.getTextNodeValue(container));

      return (new DsMessage[] {msg});  
    }

    NodeList personList = m_parser.getElements("person",element);
    NodeList groupList = m_parser.getElements("group",element);

    int length = personList.getLength() + groupList.getLength();

    if(length > 0)
    {
      DsMessage dsMsgArr[] = new DsMessage[length];

      addToArray(personList,dsMsgArr,0,type,subtype,"person");
      addToArray(
             groupList,dsMsgArr,personList.getLength(),type,subtype,"group");

      return dsMsgArr;
    }

    return null;
  }


  /**
   * Creates a dsoperation instance document from the given DsMessage and 
   * sends a SOAP request.
   *
   * @param  dsMsg  DsMessage containing request data.
   *
   * @return  DsMessage[] containing response data.
   *
   * @throws SOAPClientException an exception occurs while calling the web 
   *         service
   */

  public DsMessage[] doOperation(DsMessage dsMsg) throws SOAPClientException
  {
    Document doc = createDocument(dsMsg);
    Element response = m_soapClient.call(m_defaultEndPoint,doc); 
 
    DsMessage responseMsg[] = createDsMessage(response);

    return responseMsg;
  }


  /**
   * Set bind dn to use.
   *
   * @param  binddn  Bind DN to use.
   */

  public void setBindDN(String binddn)
  {
    m_binddn = binddn;
  }


  /**
   * Set the bind password to use.
   *
   * @param  passwd  Bind password.
   */

  public void setPassword(String passwd)
  {
    m_passwd = passwd;
  } 


  //members
  private String m_binddn = null;
  private String m_passwd = null;
  private DOMParser m_parser = null;
  private String m_templateStr = null;
  private SOAPClient m_soapClient = null;
  private String m_defaultEndPoint = null;
}
