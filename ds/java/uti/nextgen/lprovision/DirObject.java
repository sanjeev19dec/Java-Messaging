/*
 * Id: $Id: DirObject.java,v 1.4 2003/06/05 16:22:39 hannesh Exp $
 * 
 * $RCSfile: DirObject.java,v $ $Revision: 1.4 $ $Date: 2003/06/05 16:22:39 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.lprovision;

import java.util.*;

/**
 * The DirObject class provides a container for storing DirAttributes and 
 * provides methods for accessing attribute information and values.
 * <p>
 * The JNDI state and object factories provided by the lprovision API stores and
 * retrieves instances of this class to and from the directory.
 *
 * @author  Sanjeev Sharma
 */

public class DirObject extends Object
{
  /**
   * Default constructor.
   */

  public DirObject()
  {
    m_hashtable = new Hashtable();
  }


  /**
   * Add an attribute with the given properties.
   *
   * @param  name         Name of the attribute.
   * @param  required     String indicating whether or not it is a required 
   *                      attribute. Values are yes or no. 
   * @param  secret       String indicating whether this is a secret attribute
   *                      and its value must be confirmed when supplied.
   * @param  bindName     String indicating whether or not it is the attribute
   *                      to use when creating the bind name for this object.
   *                      Values are yes or no. 
   * @param  description  Short description of the attribute. 
   */

  public void addAttribute(String name, 
                           String required, 
                           String secret,
                           String bindName, 
                           String description)
  {
    m_hashtable.put(name,
                   new DirAttribute(name,required,secret,bindName,description));
  }


  /**
   * Adds the given attribute to this DirObject.
   *
   * @param  attr  DirAttribute to add.
   */

  public void addAttribute(DirAttribute attr)
  {
    m_hashtable.put(attr.getName(), attr);
  }


  /**
   * Returns the named attribute.
   *
   * @param  name  Name of attribute.
   *
   * @return  DirObject with the given name.
   */

  public DirAttribute getAttribute(String name)
  {
    return (DirAttribute)m_hashtable.get(name);
  }


  /**
   * Returns the values of the named attribute.
   *
   * @param  attrName  Name of attribute.
   *
   * @return  Vector containing the values of the named attribute.
   */

  public Vector getAttributeValues(String attrName)
  {
    DirAttribute attr = (DirAttribute)m_hashtable.get(attrName);
    return  attr.getValues();
  }


  /**
   * Returns all the attributes contained in this DirObject.
   *
   * @return  Enumeration containing all the attributes of this DirObject.
   */

  public Enumeration getAttributes()
  {
    return m_hashtable.elements();
  }


  /**
   * Returns all the names of the attributes contained in this object.
   *
   * @return  Enumeration containing all attribute names.
   */

  public Enumeration getAttributeNames()
  {
    return m_hashtable.keys();
  }


  /**
   * Sets the name of this object.
   *
   * @param  name  Name to use as the name for this object.
   */

  public void setName(String name)
  {
    m_name = name;
  }

  
  /**
   * Returns the name of this object.
   *
   * @return  String containing the name of this object.
   */

  public String getName()
  {
    return m_name;
  }


  /**
   * Returns the bind name for this DirObject
   *
   * @return  String containing the bind name for this DirObject.
   */

  public String getBindName()
  {
    String bindName = null;
    Enumeration attrs = m_hashtable.elements();

    while(attrs.hasMoreElements())
    {
      DirAttribute attr = (DirAttribute)attrs.nextElement();

      if(attr.isBindName())
      {
        if(getSubContext() != null)
        {
          bindName = new String(attr.getName()+"="+attr.getFirstValue()+","+
                                getSubContext());
        }
        else
        {
          bindName = new String(attr.getName()+"="+attr.getFirstValue());
        }
                                
        break;
      }
    }

    return bindName;
  }


  /**
   * Sets the sub context of this DirObject.  This indicates the context 
   * that the DirObject lives in.
   *
   * String  subContext  Context of this object.
   */

  public void setSubContext(String subContext)
  {
    m_subContext = subContext;
  }


  /**
   * Returns the name of the sub context this object lives in.
   *
   * @return  String containing the name of the sub context.
   */

  public String getSubContext()
  {
    return m_subContext;
  }


  /**
   * Determines if this DirObject is equal to the given instance by comparing
   * the values of the objectclass attributes.
   *
   * @param  dirObject  DirObject to compare to this.
   *
   * @return  boolean indicating the eqaulity of the DirObjects.
   */

  public boolean equals(DirObject dirObject)
  {
    int count = 0;
    Vector values = dirObject.getAttributeValues("objectclass");
    Vector thisValues = this.getAttributeValues("objectclass");
 
    if(values.size() == thisValues.size())
    {
      for(int i = 0; i < values.size(); i++)
      {
        count = 0;
        String value = ((String)values.elementAt(i)).toLowerCase();

        for(int k = 0; k < thisValues.size(); k++)
        {
          String thisValue = ((String)thisValues.elementAt(k)).toLowerCase();

          if(value.equals(thisValue))
          {
            count++;
          }
        }

        if(count == 0)
        {
          return false;
        }
      }

      return true;
    }
    else
    {
      return false;
    }
  }
          
          
  //members
  private String m_name = null;
  private String m_subContext = null;
  private Hashtable m_hashtable = null;
} 
