/*
 * Id: $Id: DirAttribute.java,v 1.4 2003/06/05 16:22:39 hannesh Exp $
 * 
 * $RCSfile: DirAttribute.java,v $ $Revision: 1.4 $ $Date: 2003/06/05 16:22:39 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.lprovision;

import java.util.*;

/**
 * Class describing a single attribute and stores the value(s) of the attribute.
 * 
 * @author  Sanjeev Sharma
 */

public class DirAttribute extends Object
{
  /**
   * Constructs a new DirAttribute with the given properties.
   *
   * @param  name         Name of the attribute. Must be equal to the name 
   *                      used in the directory.
   * @param  required     String indicating whether or not this attribute is 
   *                      required by the DirObject container.
   * @param  secret       String indicating whether this is a secret attribute 
   *                      and its value must be confirmed when supplied.
   * @param  bindName     String indicating whether or not it is the attribute
   *                      to use when creating the bind name for this object.
   *                      Values are yes or no.
   * @param  description  Short description of this attribute.
   */

  public DirAttribute(String name, 
                      String required, 
                      String secret,
                      String bindName, 
                      String description)
  {
    m_values = new Vector();
    m_name = name;
    m_description = description;
    
    if(required.equals("yes"))
    {
      m_required = true;
    }
    else
    {
      m_required = false;
    }
  
    if(secret.equals("yes"))
    {
      m_secret = true;
    }
    else
    {
      m_secret = false;
    }
  
    if(bindName.equals("yes"))
    {
      m_bindName = true;
    }
    else
    {
      m_bindName = false;
    }
  } 
 

  /**
   * Adds a value to this attribute. This method may be called multiple times. 
   *
   * @param  value  Object containing a single attribute value.
   */

  public void addValue(Object value)
  {
    m_values.add(value);
  } 


  /**
   * Returns all the values of this attribute.
   *
   * @return  Vector containing all of the values of this attribute.
   */ 

  public Vector getValues()
  {
    return m_values;
  }


  /**
   * Convenience method for returning the first value of this attribute.  Handy
   * when it is known that the attribute only contains one value.
   *
   * @return  Object containing the first value of this attribute.
   */

  public Object getFirstValue()
  {
    return m_values.elementAt(0);
  }
 

  /**
   * Indicates whether or not this is a required attribute.
   *
   * @return  boolean indicating whether or not this is a required attribute.
   */

  public boolean isRequired()
  {
    return m_required;
  } 


  /**
   * Indicates whether or not this is a secret attribute.
   *
   * @return  boolean indicating whether or not this is a secret attribute.
   */

  public boolean isSecret()
  {
    return m_secret;
  }
 

  /**
   * Indicates whether this attribute is used to create the bindName for 
   * the DirObject container it lives in.
   *
   * @return boolean
   */

  public boolean isBindName()
  {
    return m_bindName;
  }


  /**
   * Indicates whether this attribute has any values.  Returns true if this
   * attribute has no values and false if it contains one or more values.
   *
   * @return  boolean indicating whether this attribute contains any values.
   */
 
  public boolean isEmpty()
  {
    if(m_values.size() == 0)
    {
      return true;
    }
    else
    {
      return false;
    }
  }


  /**
   * Returns the name of this attribute.
   *
   * @return  String containing the name of this attribute.
   */

  public String getName()
  {
    return m_name;
  }


  /**
   * Returns the description of this attribute.
   *
   * @return  String containing a description of this object.
   */

  public String getDescription()
  {
    return m_description;
  }


  //members
  private String m_name = null;
  private Vector m_values = null;
  private boolean m_secret = false;
  private boolean m_required = false;
  private boolean m_bindName = false;
  private String m_description = null;
}
