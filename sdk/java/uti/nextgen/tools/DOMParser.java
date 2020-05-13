/*
 * Id: $Id: DOMParser.java,v 1.2 2003/08/25 22:48:53 hannesh Exp $
 * 
 * $RCSfile: DOMParser.java,v $ $Revision: 1.2 $ $Date: 2003/08/25 22:48:53 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.tools;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 * Parser class to parse and access XML documents
 *
 * @author  Sanjeev Sharma
 */

public final class DOMParser
{
  /**
   * Default constructor.
   */

  public DOMParser()
  {
  }


  /**
   * Parse an InputStream and return a document.
   *
   * @param  is  InputStream to parse.
   */

  public Document parse(InputStream is)
  {
    try
    {
      DocumentBuilderFactory docBuilderFactory = 
                            DocumentBuilderFactory.newInstance(); 

      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document xmlDoc = docBuilder.parse(is);

      return xmlDoc;
    }
    catch (SAXParseException spe)
    {
      System.out.println("Parse error at line: "+spe.getLineNumber());
      spe.printStackTrace();
    }
    catch (SAXException sxe) 
    {
      Exception  x = sxe;
      
      if (sxe.getException() != null)
      {
        x = sxe.getException();
      }

      x.printStackTrace();
    }
    catch (ParserConfigurationException pce) 
    {
      pce.printStackTrace();
    }
    catch (IOException ioe) 
    {
      ioe.printStackTrace();
    }

    return null;
  }


  /**
   * Parses an XML string and returns a Document.
   *
   * @param  xmlStr  XML string to parse.
   */

  public Document parse(String xmlStr)
  {
    ByteArrayInputStream is = new ByteArrayInputStream(xmlStr.getBytes());
    return parse(is);
  }

  /**
   * Parses the file and returns a Document.
   *
   * @param  file  File to parse.
   */

  public Document parse(File file)
  {
    try
    {
      FileInputStream is = new FileInputStream(file);
      return parse(is);
    }
    catch (IOException ioe) 
    {
      ioe.printStackTrace();
    }
    
    return null;
  }


  /**
   * Returns the element with the given name or null if it does not exist.
   *
   * @param  name  Element name.
   * @param  doc   XML document.
   */

  public Element getElement(String name, Document doc)
  {
    NodeList elementList = doc.getElementsByTagName(name);

    if(elementList.getLength() == 0)
    {
      return null;
    }
    else
    {
      return (Element)elementList.item(0);
    }
  }


  /**
   * Returns the named element inside of the given element.
   *
   * @param  elementName  The element name.
   * @param  element      The given element in which to look for the named
   *                      element.
   *
   * @return  The specified element.
   */

  public Element getElement(String elementName,Element element)
  {
    NodeList list = element.getElementsByTagName(elementName);

    if(list.getLength() == 0)
    {
      return null;
    }
    else
    {
      return (Element)(list.item(0));
    }
  }


  /**
   * Returns the children with the given name inside the given parent.
   *
   * @param  childName  Name of child elements to return.
   * @param  parent  Parent element.
   *
   * @return  NodeList of child nodes with the given name.
   */

  public NodeList getElements(String childName, Element parent)
  {
    NodeList list = parent.getElementsByTagName(childName);
    return list;
  }


  /**
   * Sets the value of the named text node.
   *
   * @param  name   Text node name.
   * @param  value  New text node value.
   * @param  doc    XML document.
   */

  public void setTextNodeValue(String name, String value, Document doc)
  {
    Element element = getElement(name,doc);

    (element.getFirstChild()).setNodeValue(value);
  }
  
  
  /**
   * Returns the value of the named text node.
   *
   * @param  name   Text node name.
   * @param  doc    XML document.
   *
   * @return  String containing the value of the named text node.
   */

  public String getTextNodeValue(String name, Document doc)
  {
    Element element = getElement(name,doc);

    return (element.getFirstChild()).getNodeValue();
  }


  /**
   * Returns the value of the given text node.
   *
   * @param  node  Given text node.
   *
   * @return  String containing the value of the given text node.
   */

  public String getTextNodeValue(Node node)
  {
    try
    {
      return (node.getFirstChild()).getNodeValue();
    }
    catch (NullPointerException ne)
    {
      return null;
    }
  }


  /**
   * Returns the value of the named text node within the given parent node.
   *
   * @param  name    Name of the text node.
   * @param  parent  Parent node.
   */

  public String getTextNodeValue(String name, Node parent)
  {
    Node node = getElement(name,(Element)parent);

    if(node != null)
    {
      return getTextNodeValue(node);
    }
    else 
    {
      return null;
    }
  }


  /**
   * Sets the value of the named attribute within the given node.
   *
   * @param  attrName  Name of attribute.
   * @param  attrVal   Attribute value.
   * @param  node      Given node.
   */

  public void setNodeValue(String attrName,String attrVal, Node node)
  {
    NamedNodeMap map = node.getAttributes();
    (map.getNamedItem(attrName)).setNodeValue(attrVal); 
  }

 
  /**
   * Sets the value of a specific attribute inside the named node of a specific
   *  DOM document.
   *
   * @param  elementName  The name of the element.
   * @param  nodeName     The name of the node.
   * @param  nodeValue    The new node value.
   * @param  document     The DOM document.
   */

  public void setNodeValue(String elementName,
                           String nodeName,
                           String nodeValue,
                           Document document)
  {
    NamedNodeMap map  =
          (document.getElementsByTagName(elementName).item(0)).getAttributes();
    (map.getNamedItem(nodeName)).setNodeValue(nodeValue);
  }


  /**
   * Sets the value of a specific attribute inside the given node.
   *
   * @param  node       The given node.
   * @param  attrName   The attribute name.
   * @param  attrValue  The new attribute value.
   */

  public void setNodeValue(Node node,
                           String attrName,
                           String attrValue)
  {
    ((node.getAttributes()).getNamedItem(attrName)).setNodeValue(attrValue);
  }


  /**
   * Returns the value of the named attribute in the named element of the given
   * document.
   *
   * @param  elementName  The element name.
   * @param  attrName     The attribute name.
   * @param  document     The given document.
   *
   * @return  A String containing the attribute value.
   */

  public String getAttrValue(String elementName,
                             String attrName,
                             Document document)
  {
    NodeList list = null;
    NamedNodeMap map = null;
    list = document.getElementsByTagName(elementName);

    if(list.getLength() > 0)
    {
      return (getAttrValue(list.item(0),attrName));
    }
    else
      return null;
  }


  /**
   * Returns the value of the named attribute in the given node.
   *
   * @param  node      The given node.
   * @param  attrName  The attribute name.
   *
   * @return  A String containing the attribute value.
   */

  public String getAttrValue(Node node,
                             String attrName)
  {
    NamedNodeMap map = null;
    map = node.getAttributes();

    if(map != null)
    {
      Node attr = map.getNamedItem(attrName);

      if(attr == null)
        return null;
      else
        return (attr.getNodeValue());
    }
    else
     return null;
  }


  /**
   * Sets the value of a text node.
   *
   * @param  node   The text node.
   * @param  value  The new value of the text node.
   */

  public void setTextNodeValue(Node node,
                               String value)
  {
    node.getFirstChild().setNodeValue(value);
  }


  /**
   * Returns a vector of nodes that have the same name and contains a named
   * attribute with the same value.
   *
   * @param  elementName  Name of the element to look for.
   * @param  attrName     Name of attribute.
   * @param  attrValue    Value of attribute.
   * @param  parent       Parent node of elementName.
   *
   * @return  Vector containing result nodes.
   */

  public Vector getElementsByAttrValue(String elementName,
                                       String attrName,
                                       String attrValue,
                                       Element parent)
  {
    Vector vector = new Vector();
    NodeList list = getElements(elementName,parent);

    for(int i = 0; i < list.getLength(); i++)
    {
      Node node = list.item(i);
      String val = getAttrValue(node,attrName);

      if(val.equals(attrValue))
      {
        vector.add(node);
      }
    }

    return vector;
  } 
 

  /**
   * Converts the given document to an XML string.
   *
   * @param  doc  Document to convert.
   */

  public String getXMLStr(Document doc)
  {
    try
    {
      TransformerFactory tfactory = TransformerFactory.newInstance();
      Transformer transformer = tfactory.newTransformer();

      ByteArrayOutputStream os = new ByteArrayOutputStream();

      transformer.transform(new DOMSource(doc),
                            new StreamResult(os));

      return os.toString();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return null;
  }
}
