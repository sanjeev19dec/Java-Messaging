/*
 * Id: $Id: LProvisionFactory.java,v 1.4 2003/07/14 11:33:45 hannesh Exp $
 * 
 * $RCSfile: LProvisionFactory.java,v $ $Revision: 1.4 $ $Date: 2003/07/14 11:33:45 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.lprovision;


import java.util.*;
import org.w3c.dom.*;
import uti.nextgen.tools.*;

/**
 * Convenience factory for obtaining DirObject instances.  
 * <p>
 * The factory class handles all access to the XML configuration regarding 
 * instantiation of DirObjects.  
 * <p>
 * The DirObjects that can be provided by this factory is determined by the
 * configuration file used to initialise the factory.
 *
 * @author  Sanjeev Sharma
 */

public class LProvisionFactory extends Object
{
  /**
   * Sets the XML configuration properites.
   *
   * @param  xmlProps  XML configuration properties.
   */

  public static void init(XMLProperties xmlProps)
  {
    m_xmlProps = xmlProps;
  }


  /**
   * Returns the dirObject with the given name.
   *
   * @param  objName  Name of DirObject to return.
   *
   * @throws  ConfException if the DirObject could not be created.
   */

  public static DirObject getDirObject(String objName) throws ConfException
  {
    Element objectElement = m_xmlProps.getElementByNameAttr("DirObject",
                                                             objName);
    String subContext = m_xmlProps.getAttributeValue(objectElement,
                                                     "subcontext"); 
    DirObject dirObject = new DirObject();
    dirObject.setName(objName);
    dirObject.setSubContext(subContext.length() == 0 ? null : subContext);

    NodeList objectClassList = m_xmlProps.getElements(
                           m_xmlProps.getElement(objectElement,"ObjectClasses"),
                           "ObjectClass");

    DirAttribute objectClassAttr = new DirAttribute(
                                           "objectclass","yes","no","no",null);

    for(int j = 0; j < objectClassList.getLength(); j++)
    {
      objectClassAttr.addValue(
                     m_xmlProps.getTextNodeValue(objectClassList.item(j))); 
    }

    dirObject.addAttribute(objectClassAttr);
      
    NodeList attrList = m_xmlProps.getElements(
                              m_xmlProps.getElement(objectElement,"Attributes"),
                              "Attribute");

    for(int k = 0; k < attrList.getLength(); k++)
    {
      Node attrNode = attrList.item(k);

      DirAttribute dirAttr = new DirAttribute(
                 m_xmlProps.getAttributeValue((Element)attrNode,"name"),
                 m_xmlProps.getAttributeValue((Element)attrNode,"required"),
                 m_xmlProps.getAttributeValue((Element)attrNode,"secret"),
                 m_xmlProps.getAttributeValue((Element)attrNode,"bindname"),
                 m_xmlProps.getTextNodeValue((Element)attrNode,"Description"));

      try
      {
        NodeList valuesList = m_xmlProps.getElements((Element)attrNode,"Value");

        for(int l = 0; l < valuesList.getLength(); l++)
        {
          dirAttr.addValue(m_xmlProps.getTextNodeValue(valuesList.item(l)));
        }
      }
      catch (ConfException ce)
      {
      }

      dirObject.addAttribute(dirAttr);
    }

    return dirObject;
  }


  /**
   * Returns an enumeration of the names of the DirObjects that can be provided
   * by the factory.
   *
   * @return  Enumeration of DirObject names.
   *
   * @throws  ConfException the name cannot be retrieved.
   */

  public static Enumeration getDirObjectNames() throws ConfException
  {
    Vector namesVector = new Vector();
    NodeList objectList = m_xmlProps.getElements(
                              m_xmlProps.getElement("DirObjects"),"DirObject");

    for(int i = 0; i < objectList.getLength(); i++)
    {
      namesVector.add(m_xmlProps.getAttributeValue((Element)objectList.item(i),
                                                   "name"));
    }

    return namesVector.elements();
  }


  //members
  private static XMLProperties m_xmlProps = null;
}
