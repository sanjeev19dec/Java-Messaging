/*
 * Id: $Id: XMLProperties.java,v 1.1 2003/05/27 15:01:07 hannesh Exp $
 * 
 * $RCSfile: XMLProperties.java,v $ $Revision: 1.1 $ $Date: 2003/05/27 15:01:07 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.tools;

import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

/**
 * Provides access to XML configuration properties.  Provides some convenience
 * methods that provide easier access to XML elements and attributes.
 *
 * @author  Sanjeev Sharma
 */

public class XMLProperties extends Object
{
  /**
   * Constructs a new XMLProperties object from teh given InputStream.
   *
   * @param  is  InputStream containing XML data.
   *
   * @throws ConfException if the InputStream cannot be parsed.
   */

  public XMLProperties(InputStream is) throws ConfException
  {
    try
    {
      DocumentBuilderFactory docBldrFactory = 
                                    DocumentBuilderFactory.newInstance();
      DocumentBuilder docBldr = docBldrFactory.newDocumentBuilder();
      m_document = docBldr.parse(is);
      is.close();
    }
    catch (SAXException se)
    {
      throw new ConfException(se);
    }
    catch (IOException ioe)
    {
      throw new ConfException(ioe);
    }
    catch (ParserConfigurationException pce)
    {
      throw new ConfException(pce);
    }
  }


  /**
   * Constructs a new XMLProperties object from the given filename.
   *
   * @param  filename  Name of file containing XML data.
   *  
   * @throws  ConfException if an exception occurs while parsing the XML
   *          document.
   * @throws  IOException if the file does not exist.
   */

  public XMLProperties(String filename) throws ConfException,
                                               IOException
  {
    this(new FileInputStream(filename));
  }


  /**
   * Returns the element with the given name.
   *
   * @param  name  Name of the XML element that must be returned.
   *
   * @return  Element with the given name.
   *
   * @throws  ConfException if the element cannot be found
   */

  public Element getElement(String name) throws ConfException
  {
    NodeList elementList = m_document.getElementsByTagName(name);

    if(elementList.getLength() == 0)
    {
      throw new ConfException("Element does not exist: ["+name+"]");
    }
    else
    {
      return (Element)elementList.item(0);
    }
  }


 /**
  * Returns the Element with the given name and "name attribute" with the 
  * given value.
  *
  * @param  elementName  Name of the element to return.
  * @param  nameAttrVal  Value of the name attribute.
  *
  * @return  Element
  *
  * @throws  ConfException if the element could not be found.
  */

  public Element getElementByNameAttr(String elementName,
                                      String nameAttrVal) throws ConfException
  {
    NodeList list = m_document.getElementsByTagName(elementName);

    for(int i = 0; i < list.getLength(); i++)
    {
      String value = null;
      value = getAttributeValue((Element)list.item(i),"name");

      if(value.equals(nameAttrVal))
      {
        return (Element)list.item(i);
      }
    }

    throw new ConfException("Unable to find element ["+elementName+"] with "+
                            "name attribute = ["+nameAttrVal+"]");
  }


  /**
   * Returns the element within the named parent element.
   *
   * @param  parentName  Name of the parent element.
   * @param  name        Name of the element that must be returned.
   *
   * @return  Element with the given name. 
   *
   * @throws  ConfException if the named elements does not exist.
   */

  public Element getElement(String parentName, String name) throws ConfException
  {
    Element parent = getElement(parentName);
    NodeList elementList = parent.getElementsByTagName(name);

    if(elementList.getLength() == 0)
    {
      throw new ConfException("Element ["+name+"] does not exist within "+
                              "element ["+parentName+"]");
    }
    else
    {
      return (Element)elementList.item(0);
    }
  }


  /**
   * Returns the named child element within the given parent element.
   *
   * @param  parent  Parent element.
   * @param  name    Name of child element.
   *
   * @return  Element with the given name.
   *
   * @throws  ConfException if the parent does not contain the named child
   *          element.
   */

  public Element getElement(Element parent, String name) throws ConfException
  {
    NodeList elementList = parent.getElementsByTagName(name);

    if(elementList.getLength() == 0)
    {
      throw new ConfException("Element ["+name+"] does not exist within "+
                              "element ["+parent.getNodeName()+"]");
    }
    else
    {
      return (Element)elementList.item(0);
    }
  }


  /**
   * Returns all the child nodes with the given name within the given parent
   * element.
   *
   * @param  parent  Parent element containing the named children.
   * @param  name    Name of the child nodes to return. If null, returns all
   *                 child nodes.
   *
   * @return  NodeList containing the child nodes.
   *
   * @throws  ConfException if the parent does not contain any children with the
   *          given name.
   */ 

  public NodeList getElements(Element parent, String name) throws ConfException
  {
    if(name == null)
    {
      return parent.getChildNodes();
    }
    else
    {
      NodeList elementList = parent.getElementsByTagName(name);

      if(elementList.getLength() == 0)
      {
        throw new ConfException("Element ["+parent.getNodeName()+"] has no "+
                                "children named: ["+name+"]");
      }
      else
      {
        return elementList;
      }
    }
  }


  /**
   * Returns the value of the given text node.
   *
   * @param  textNode  The given text node.
   *
   * @return  String containing the value of the text node. Null if the node 
   *          has no value.
   */

  public String getTextNodeValue(Node textNode)
  {
    boolean hasChild = textNode.hasChildNodes();

    if(hasChild)
    {
      return ((textNode.getFirstChild()).getNodeValue()).trim();
    }
    else
    {
      return null;
    }
  }


  /**
   * Returns the value of the named text node within the given parent element.
   *
   * @param  parent  Parent element.
   * @param  name    Name of the text node.
   *
   * @return  String containing the value of the named text node or null if
   *          the named node is not a text node.
   *
   * @throws  ConfException if the named node does not exist
   */

  public String getTextNodeValue(Element parent, 
                                 String name) throws ConfException
  {
    Element textNode =  getElement(parent,name);
    String value = getTextNodeValue(textNode);

     return value;
  }


  /**
   * Returns the value of the named attribute within the given element.
   *
   * @param  element   Given element.
   * @param  name  Attribute name.
   *
   * @return  String containing the value of the named attribute. Null if the
   *          attribute has no value.
   *
   * @throws  ConfException if the attribute does not exist.
   */

  public String getAttributeValue(Element element, 
                                  String name) throws ConfException
  {
    NamedNodeMap attrs = element.getAttributes();
    Node attr = attrs.getNamedItem(name);

    if(attr == null)
    {
      throw new ConfException("Attribute ["+name+"] is not an attribute of "+
                              "element ["+element.getNodeName()+"]");
    }
    else
    {
      String value = attr.getNodeValue();
  
      if(value.length() == 0)
      {
        return null;
      }
      else
      {
        return value;
      }
    }
  }
                              

  /**
   * Returns the members document fort this XMLProperties object.
   *
   * @return  Document containing the XML configuration.
   */

  public Document getDocument()
  {
    return m_document;
  }


  //members
  private Document m_document = null;
}
