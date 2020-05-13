/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.ds;

import java.util.*;

/**
 * Encapsulates information regarding a single dsoperation element 
 * (group or person or error). 
 *
 * @author  Sanjeev Sharma
 */

public class DsMessage extends Object
{
  /**
   * Default constructor
   */

  public DsMessage()
  { 
    m_attrs = new LinkedHashMap();
    m_retAttrs = new LinkedList();
  }


  /**
   * Sets the type of this message.  See the dsoperation schema for details.
   *
   * @param  type  Type of this message.
   */

  public void setType(String type)
  {
    m_type = type;
  }


  /**
   * Returns the type of this message.  See dsoperation schema for details.
   *
   * @return  String indicating the type of this message.
   */

  public String getType()
  {
    return m_type;
  }
 

  /**
   * Sets the object type of this message. Types are: person | group | error 
   *
   * @param  objType  Object type of this message.
   */

  public void setObjectType(String objType)
  {
    m_objType = objType;
  }


  /**
   * Returns the object type of this message. Types are: person | group | error 
   *
   * @return  String indicating the object type of this message.
   */

  public String getObjectType()
  {
    return m_objType;
  }


  /**
   * Sets the subtype of this message.  See the dsoperation schema for details.
   *
   * @param  subtype  subtype of this message.
   */

  public void setSubtype(String subtype)
  {
    m_subtype = subtype;
  }


  /**
   * Returns the subtype of this message.  See dsoperation schema for details.
   *
   * @return  String indicating the subtype of this message.
   */

  public String getSubtype()
  {
    return m_subtype;
  }


  /**
   * Adds an attribute with a single value to this message
   *
   * @param  name   Attribute name.
   * @param  value  Attribute value. 
   */

  public void addAttribute(String name, String value)
  {
    m_attrs.put(name,value);
  }


  /**
   * Adds an attribute with a multiple values to this message
   *
   * @param  name    Attribute name.
   * @param  values  Attribute values. 
   */

  public void addAttribute(String name, Vector values)
  {
    m_attrs.put(name,values);
  }


  /**
   * Adds a return attribute name to this message.
   *
   * @param  name  Name of return attribute.
   */

  public void addReturnAttributeName(String name)
  {
    m_retAttrs.add(name);
  }


  /**
   * Returns the list of return attribute names for this message.
   *
   * @return  LinkedList containing return attribute names.
   */

  public LinkedList getReturnAttributeNames()
  {
    return m_retAttrs;
  }


  /**
   * Returns the value of the named attribute.
   *
   * @param  name  Name of attribute to return.
   *
   * @return  Object containing the value of the named attribute.
   */

  public Object getAttributeValue(String name)
  {
    return m_attrs.get(name);
  }


  /**
   * Returns a String array of the attribute names of this message.
   *
   * @return  String array containing attribute names.
   */

  public String[] getAttributeNames()
  {
    Set keySet = m_attrs.keySet();
    String names[] = new String[m_attrs.size()];

    keySet.toArray(names);

    return names;
  }


  /**
   * Returns a LinkedList containing all the values of all the attributes
   * of this message.
   *
   * @return  LinkedList of attribute values.
   */

  public LinkedList getAttributeValues()
  {
    return (new LinkedList(m_attrs.values()));
  }


  //members
  private String m_type = null;
  private String m_subtype = null;
  private String m_objType = null;
  private LinkedHashMap m_attrs = null;
  private LinkedList m_retAttrs = null;
}
