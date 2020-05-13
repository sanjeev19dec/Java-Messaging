/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.tools;

import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 * Convenience methods for handling DOM documents.
 *
 * @author  Sanjeev Sharma
 */

public class DomImpl extends Object
{
  /**
   * Empty constructor for the DomImpl.
   */

  public DomImpl()
  {
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
   * Sets the value of a text node with a given name in a specific DOM document.
   *
   * @param  nodeName   The name of the text node.
   * @param  nodeValue  The new node value.
   * @param  doc        The document to find the text node in.
   */

  public void setTextNodeValue(String nodeName,
                               String nodeValue,
                               Document doc)
  {
    setTextNodeValue(getElement(nodeName,doc),nodeValue);
  }


  /**
   * Returns the value of the given text node; null will be returned if the node
   * does not have a value.
   *
   * @param  node  The text node.
   */

  public String getTextNodeValue(Node node)
  {
    try
    {
      return (node.getFirstChild().getNodeValue());
    }
    catch (Exception e)
    {
      return null;
    }
  }


  /**
   * Returns the value of the text node with the given name within the specified
   * document. Returns null if the text node does not have a value.
   *
   * @param  nodeName  The given text node name.
   * @param  doc       The document to find the text node in.
   */

  public String getTextNodeValue(String nodeName,
                                 Document doc)
  {
    try
    {
      return (getElement(nodeName,doc).getFirstChild()).getNodeValue();
    }
    catch (Exception de)
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

  public String getTextNodeValue(String name,
  									Element parent) throws ConfException
  {
    Node textNode =  getElement(name,parent);
    String value = getTextNodeValue(textNode);

     return value;
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
   * Returns the named element inside of the given element.
   *
   * @param  elementName  The element name.
   * @param  element      The given element in which to look for the named
   *                      element.
   *
   * @return  The specified node.
   */

  public Node getElement(String elementName,
                         Element element)
  {
    return ((element.getElementsByTagName(elementName)).item(0));
  }


  /**
   * Returns the named element inside of the given document.
   *
   * @param  elementName  The element name.
   * @param  document     The given document.
   *
   * @return  The specified node.
   */

  public Node getElement(String elementName,
                         Document document)
  {
    return ((document.getElementsByTagName(elementName)).item(0));
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
