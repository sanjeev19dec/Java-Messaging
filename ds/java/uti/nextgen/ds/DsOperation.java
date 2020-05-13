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
import javax.naming.*;
import uti.nextgen.tools.*;
import uti.nextgen.lprovision.*;

/**
 * Class that executes directory operations based on dsoperation instance 
 * documents.
 *
 * @author  Sanjeev Sharma
 */

public class DsOperation extends Object
{
  /**
   * Creates a new DsOperation instance
   *
   * @param  xmlConf  Location of XML configuration file.
   */

  public DsOperation(String xmlConf) throws ConfException,
                                            IOException,
                                            NamingException
  {
    XMLProperties xmlProps = new XMLProperties(xmlConf);

    String provisionConf =
              xmlProps.getTextNodeValue(xmlProps.getElement("ProvisionConf"));

    m_stdResponse = initTemplateStr(
          xmlProps.getTextNodeValue(xmlProps.getElement("StdResponse")));
    m_searchResponse = initTemplateStr(
            xmlProps.getTextNodeValue(xmlProps.getElement("SearchResponse")));

    Element search = xmlProps.getElement("Search");
    Element person = xmlProps.getElement(search,"Person");

    NodeList pAttrs = xmlProps.getElements(person,"attr");
    m_personAttrs = new Vector();

    for(int i = 0; i < pAttrs.getLength(); i++)
    {
      m_personAttrs.add(xmlProps.getTextNodeValue(pAttrs.item(i)));
    }

    Element group = xmlProps.getElement(search,"Group");
    NodeList gAttrs = xmlProps.getElements(group,"attr");
    m_groupAttrs = new Vector();

    for(int i = 0; i < gAttrs.getLength(); i++)
    {
      m_groupAttrs.add(xmlProps.getTextNodeValue(gAttrs.item(i)));
    }

    Element delete = xmlProps.getElement("Delete");
    person = xmlProps.getElement(delete,"Person");
    m_personDeleteAttr = xmlProps.getTextNodeValue(person,"attr");

    group = xmlProps.getElement(delete,"Group");
    m_groupDeleteAttr = xmlProps.getTextNodeValue(group,"attr");

    m_context = new LProvisionContext(provisionConf);
    m_parser = new DOMParser();
  }


  /**
   * Reads the named file and returns content as string.
   *
   * @param  fileName  File to read.
   *
   * @return  String containing file data.
   *
   * @throws  IOException if the file could not found or read
   */

  private String initTemplateStr(String fileName) throws IOException
  {
    File file = new File(fileName);
    FileInputStream fis = new FileInputStream(file);

    byte data[] = new byte[(int)file.length()];
    fis.read(data);
    fis.close();

    return new String(data);
  }   


  /**
   * Decides which action was requested and invokes the corrrect method.
   *
   * @param  operation   XML string containing operation to execute.
   *
   * @return  String containing XML indicating the result of the operation.
   */

  public String doOperation(String operation)
  {
    String returnStr = null;
    Document doc = m_parser.parse(operation);
    Element opElement = doc.getDocumentElement();

    String action = m_parser.getAttrValue("action","type",doc);

    try
    {
      Element credentials = m_parser.getElement("credentials",doc);
      String dn = m_parser.getTextNodeValue("binddn",credentials);
      String passwd = m_parser.getTextNodeValue("password",credentials);

      if(dn != null)
      {
        m_context.setBindDn(dn);
      }
      else
      {
        throw new NamingException("Bind DN is null");
      }

      if(passwd != null)
      {
        m_context.setPassword(passwd);
      }
      else
      {
        throw new NamingException("Bind password is null");
      }

      m_context.init();
    }
    catch (NamingException ne)
    {
      return buildStdResponse(action,"none",ne.getMessage());
    }


    if(action.equals("add") || action.equals("modify"))
    {
      returnStr = bind(doc,action);
    }
    else if(action.equals("search"))
    {
      returnStr = search(doc); 
    }
    else
    {
      returnStr = delete(doc);
    }

    try
    {
      m_context.close();
    }
    catch (NamingException ne)
    {
      ne.printStackTrace();
    }

    return returnStr;
  } 


  /**
   * Handles all delete operations.
   *
   * @param  doc  XML document containing delete information.
   *
   * @return  String containing result of delete operation.
   */

  private String delete(Document doc)
  {
    NodeList list = doc.getDocumentElement().getChildNodes();

    for(int i = 0; i < list.getLength(); i++)
    {
      Node node = list.item(i);
      String nodeName = node.getNodeName();

      if((nodeName.equals("#text")) ||
         (nodeName.equals("credentials")) ||
         (nodeName.equals("action")) ||
         (nodeName.equals("error")))
      {
        continue;
      }
      else
      {
        DirObject dirObj = null;
        String deleteAttr = null;

        try
        {
          if(nodeName.equals("person"))
          {
            dirObj = LProvisionFactory.getDirObject("user");
            deleteAttr = m_personDeleteAttr; 
          }
          else
          {
            dirObj = LProvisionFactory.getDirObject("group");
            deleteAttr = m_groupDeleteAttr; 
          }
          
        }
        catch (ConfException ce)
        {
          ce.printStackTrace();
        }

        try
        {
          String deleteAttrVal = m_parser.getTextNodeValue(deleteAttr,node);

          m_context.delete(deleteAttr+"="+
                           deleteAttrVal+","+
                           dirObj.getSubContext());
        }
        catch (NamingException ne)
        {
          return buildStdResponse("delete","none",ne.getMessage());
        }
      }
    }

    return buildStdResponse("delete","none","success");
  }


  /**
   * Handles add and modify operations
   *
   * @param  doc      XML document containing bind information.
   * @param  action   The type of action.
   *
   * @return  String containing XML indicating the result of the operation.
   */

  private String bind(Document doc, String action)
  {
    String subType = m_parser.getAttrValue("action","subtype",doc);
    NodeList list = doc.getDocumentElement().getChildNodes();

    for(int i = 0; i < list.getLength(); i++)
    {
      Node node = list.item(i);
      String nodeName = node.getNodeName();

      if((nodeName.equals("#text")) || 
         (nodeName.equals("credentials")) ||
         (nodeName.equals("action")) ||
         (nodeName.equals("error")))
      {
        continue;
      }
      else
      {
        String val = null;
        DirObject dirObj = null;

        try
        {
          if(nodeName.equals("person"))
          {
            dirObj = LProvisionFactory.getDirObject("user");
          }
          else
          {
            dirObj = LProvisionFactory.getDirObject("group");
          }
        }
        catch (ConfException ce)
        {
          ce.printStackTrace();
        }

        NodeList details = node.getChildNodes();

        for(int j = 0; j < details.getLength(); j++)
        {
          Node detailNode = details.item(j);
          String name = detailNode.getNodeName();
        
          if(!(name.equals("#text")))
          {
            val = m_parser.getTextNodeValue(detailNode);

            if(val != null)
            {
              DirAttribute attr = dirObj.getAttribute(name);
              attr.addValue(val);
            }
          }
        }

        try
        {
          if(action.equals("add"))
          {
            m_context.add(dirObj);
          }
          else if(action.equals("modify"))
          {
            if(subType.equals("rebind"))
            {
              m_context.update(dirObj);
            }
            else if(!(subType.equals("none")))
            {
              m_context.modify(subType,dirObj);
            }
          }
        }
        catch (NamingException ne)
        {
          return buildStdResponse(action,subType,ne.getMessage());
        }
      }
    }

    return buildStdResponse(action,subType,"success");
  } 


  /**
   * Creates the appropriate template based on the given value.
   *
   * @param  type  Type of template: person, group or std.
   *
   * @return  DOM document instance.
   */

  private Document createTemplate(String type)
  {
    File file = null;

    if(type.equals("search"))
    {
      return m_parser.parse(m_searchResponse);
    }
    else
    {
      return m_parser.parse(m_stdResponse);
    }
  }


  /**
   * Clone the element with the given name.
   *
   * @param  name  Name of element to clone.
   * @param  doc   Document containing named element.
   *
   * @return  Element containing a clone of the named element.
   */

  private Element getClone(String name,Document doc)
  {
    Node node = m_parser.getElement(name,doc);
    return (Element)node.cloneNode(true);
  }


  /**
   * Builds a valid dsoperation instance based on the given action and message.
   *
   * @param  action   Current action.
   * @param  subType  Action sub type.
   * @param  message  Message to use.
   *
   * @return  String containing a dsoperation instance document.
   */

  private String buildStdResponse(String action,String subType, String message)
  {
    Document doc = createTemplate("std");
    Element actionElement = m_parser.getElement("action",doc);
    m_parser.setNodeValue(actionElement,"type",action);
    m_parser.setNodeValue(actionElement,"subtype",subType);

    Element errElement = m_parser.getElement("error",doc);
    errElement.appendChild(doc.createTextNode(message));

    return m_parser.getXMLStr(doc);
  }


  /**
   * Generates a search filter based on the contents of a dsoperation document
   * instance.
   *
   * @param  type  Type of filter to create: person or group.
   * @param  node  Node containing data for filter.
   * @param  doc   dsoperation instance document.
   *
   * @return  String containing a valid search filter.
   */

  private String getFilter(String type,Node node, Document doc)
  {
    Vector localVector = null;

    if(type.equals("person"))
    {
      localVector = m_personAttrs;
    }
    else
    {
      localVector = m_groupAttrs;
    }
 
    for(int i = 0; i < localVector.size(); i++)
    {
      String attrName = (String)localVector.elementAt(i);
      String attrVal = m_parser.getTextNodeValue(attrName,node); 

      if(attrVal != null)
      {
        return attrName+"="+attrVal;
      }
    }

    return null;
  }


  /**
   * Builds a valid dsoperation instance based on the contents of an Enumeration
   * containing DirObject instances.
   *
   * @param  vector  Vector of Enumerations containing search results.
   *
   * @return  String containing a dsoperation instance document.
   */

  private String buildSearchResponse(Vector vector)
  {
    Document doc = createTemplate("search");
    Element action = m_parser.getElement("action",doc);
    m_parser.setNodeValue(action,"type","search");
    m_parser.setNodeValue(action,"subtype","none");
   
    for(int k = 0; k < vector.size(); k++)
    {
      Enumeration enum = (Enumeration)vector.elementAt(k);

      while(enum.hasMoreElements())
      {
        DirObject dirObj = (DirObject)enum.nextElement();
        Element container = 
           getClone(dirObj.getName().equals("user") ? "person" : "group" ,doc);

        m_parser.setNodeValue("dn",m_context.getDN(dirObj),container);

        Enumeration attrs = dirObj.getAttributes();

        while(attrs.hasMoreElements())
        {
          DirAttribute attr = (DirAttribute)attrs.nextElement();
          String name = attr.getName();

          if((!(attr.isEmpty())) && (!(name.equals("objectclass"))))
          {
            Element attrElement = m_parser.getElement(name,container);
            Vector values = attr.getValues();

            for(int i = 0; i < values.size(); i++)
            { 
              Object valObj = values.elementAt(i);
              Element attrClone = getClone(name,doc);
            
              if(valObj instanceof byte[])
              {
                attrClone.appendChild(
                           doc.createTextNode(new String((byte[])valObj)));
              }
              else
              {
                attrClone.appendChild(doc.createTextNode(valObj.toString()));
              }

              container.insertBefore(attrClone,attrElement.getNextSibling());
            }
         
            container.removeChild(attrElement);
          }
        }

        String containerName = container.getNodeName();

        if(containerName.equals("person"))
        {
          doc.getDocumentElement().insertBefore(
                                          container,
                                          m_parser.getElement("group",doc));
        }
        else
        {
          doc.getDocumentElement().appendChild(container);
        }
      }
    }

    Element origPersonContainer = m_parser.getElement("person",doc);
    doc.getDocumentElement().removeChild(origPersonContainer); 

    Element origGroupContainer = m_parser.getElement("group",doc);
    doc.getDocumentElement().removeChild(origGroupContainer); 

    return m_parser.getXMLStr(doc);
  }

  
  /**
   * Executes the context search method and returns a valid dsoperation instance
   * document.
   *
   * @param  doc   dsoperation instance received.
   *
   * @param  String containing a valid dsoperation XML string.
   */

  private String search(Document doc)
  {
    Vector results = new Vector();

    Element returnElement = m_parser.getElement("return",doc);
    String returnAttrs[] = null;

    if(returnElement != null)
    {
      NodeList returnAttrList = 
           m_parser.getElements("attribute",returnElement);

      returnAttrs = new String[returnAttrList.getLength()];

      for(int k = 0; k < returnAttrList.getLength(); k++)
      {
        returnAttrs[k] = m_parser.getTextNodeValue(returnAttrList.item(k));
      }
    }

    NodeList list = doc.getDocumentElement().getChildNodes();

    for(int i = 0; i < list.getLength(); i++)
    {
      Node node = list.item(i);
      String nodeName = node.getNodeName();

      if((nodeName.equals("#text")) || 
         (nodeName.equals("credentials")) ||
         (nodeName.equals("action")) ||
         (nodeName.equals("error")))
      {
        continue;
      }
      else
      {
        String filter = getFilter(nodeName,node,doc);

        if(filter != null)
        {
          try
          {
            DirObject obj = 
             LProvisionFactory.getDirObject( 
                               nodeName.equals("person") ? "user" : "group");

            Enumeration enum = m_context.search(obj.getSubContext(),
                                                filter,
                                                returnAttrs);
            results.add(enum);
          }
          catch (NamingException ne)
          {
            return buildStdResponse("search","none",ne.getMessage()); 
          }
          catch (ConfException ce)
          {
            return buildStdResponse("search","none",ce.getMessage());
          }
        }
      }
    }
            
    String retXml = buildSearchResponse(results);
    return retXml;
  }


  //member
  private String m_stdResponse = null;
  private String m_searchResponse = null;
  private String m_groupDeleteAttr = null;
  private String m_personDeleteAttr = null;
  private DOMParser m_parser = null;
  private Vector m_groupAttrs = null;
  private Vector m_personAttrs = null;
  private LProvisionContext m_context = null;
}
