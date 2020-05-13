/*
 * Id: $Id: LProvisionContext.java,v 1.7 2003/08/27 10:19:04 hannesh Exp $
 * 
 * $RCSfile: LProvisionContext.java,v $ $Revision: 1.7 $ $Date: 2003/08/27 10:19:04 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.lprovision;

import java.io.*;
import java.util.*;
import javax.naming.*;
import uti.nextgen.tools.*;
import javax.naming.directory.*;

/**
 * Provides a layer of abstraction for the JNDI calls used to commit and read
 * DirObjects from the directory.
 * <p>
 * Intialises the LProvisionFactory and the JNDI configuration.
 *
 * @author  Sanjeev Sharma
 */

public class LProvisionContext extends Object
{
  /**
   * Initialise the context by initialising the LProvisionFactory and the JNDI 
   * configuration.
   *
   * @param  xmlProps  XML configuration properties.
   *
   * @throws  ConfException if the context could not be initialised.
   * @throws  IOException if the configuration file could not be read.
   * @throws  NamingException if the JNDI configuration could not be created.
   */

  public LProvisionContext(XMLProperties xmlProps) throws ConfException,
                                                          IOException,
                                                          NamingException
  {
    LProvisionFactory.init(xmlProps);
    m_jndiConfig = new JNDIConfig(xmlProps);
  }


  /**
   * Initialises the context with the name of the XML configuraration file.
   *
   * @param  xmlFile  XML configuration properties file name.
   *
   * @throws  ConfException if the context could not be initialised.
   * @throws  IOException if the configuration file could not be read.
   * @throws  NamingException if the JNDI configuration could not be created.
   */

  public LProvisionContext(String xmlFile) throws ConfException,
                                                  IOException,
                                                  NamingException
  {
    this(new XMLProperties(xmlFile));
  }


  /**
   * Initialise the JNDI connection.
   *
   * @throws  NamingException if the JNDI connection cannot be created.
   */

  public void init() throws NamingException
  {
    m_jndiConfig.createDirContext();
  }


  /**
   * Set the Provider URL.  This is the URL that will be used to connect to
   * the LDAP server.  It must include the base dn aswell. For example:
   * ldap://someserver:389/ou=People,dc=foo,dc=com.
   *
   * @param  url  Provider URL to use.
   */

  public void setProviderURL(String url)
  {
    m_jndiConfig.setProviderUrl(url);
  }


  /**
   * Set the bind dn.
   *
   * @param  bindDn  dn to use when creating the JNDI connection.
   */

  public void setBindDn(String bindDn)
  {
    m_jndiConfig.setPrincipal(bindDn);
  }


  /**
   * Set the password to use when binding.
   *
   * @param  passwd  Password to use when binding.
   */

  public void setPassword(String passwd)
  {
    m_jndiConfig.setCredentials(passwd);
  }


  /**
   * Builds a hashtable of variable names and their corresponding attribute
   * names and replace all occurences of the variables with the correct
   * attribute value.
   *
   * @param  dirObject  Object to perform substitution on.
   */

  private void doVarSubstitution(DirObject dirObject)
  {
    Hashtable varTable = new Hashtable();
    Enumeration attrNames = dirObject.getAttributeNames();

    while(attrNames.hasMoreElements())
    {
      String attrName = (String)attrNames.nextElement();
      varTable.put("\\{"+attrName+"\\}",attrName);
    }

    Enumeration vars = varTable.keys();
    
    while(vars.hasMoreElements())
    {
      String var = (String)vars.nextElement();
      String attrName = (String)varTable.get(var);
      
      Enumeration attrs = dirObject.getAttributes();

      while(attrs.hasMoreElements())
      {
        DirAttribute attr = (DirAttribute)attrs.nextElement();

        if((attr.getName()).equals(attrName))
        {
          continue;
        }
        else
        {
          Vector values = attr.getValues();
      
          for(int i = 0; i < values.size(); i++)
          {
            Object obj = values.elementAt(i);

            if(obj instanceof String)
            {
              String val = (String)obj;

              if(!(dirObject.getAttribute(attrName).isEmpty()))
              {
                Object attrVal = 
                         (dirObject.getAttribute(attrName)).getFirstValue();

                if(attrVal instanceof String)
                {
                  val = val.replaceAll(
                    var,
                    (String)attrVal);

                  values.setElementAt(val,i);
                }
              }
            }
          }
        }
      }   
    }
  }


  /**
   * Commits the given DirObject to the directory.
   *
   * @param  dirObject  DirObject to commit to the directory.
   *
   * @throws  NamingException if the object could not be committed to the 
   *          directory.
   */

  public void commit(DirObject dirObject) throws NamingException
  {
    DirContext dirContext = m_jndiConfig.getDirContext();

    doVarSubstitution(dirObject);
    dirContext.rebind(dirObject.getBindName(),dirObject); 
  }

 
  /**
   * Updates the given DirObject in the directory.
   *
   * @param  dirObject  DirObject to update in the directory.
   *
   * @throws  NamingException if the object could not be updated in the 
   *          directory.
   */

  public void update(DirObject dirObject) throws NamingException
  {
    DirContext dirContext = m_jndiConfig.getDirContext();

    doVarSubstitution(dirObject);
    dirContext.rebind(dirObject.getBindName(),dirObject); 
  }


  /**
   * Modify the directory entry to reflect the values contained in the  given
   * DirObject.
   *
   * @param  modType    Modification type. "add | replace | remove"
   * @param  dirObject  DirObject containing all attributes to modify.
   *
   * @throws  NamingException if the modifcations cannot be made.
   */

  public void modify(String modType,DirObject dirObject) throws NamingException
  {
    DirContext dirContext = m_jndiConfig.getDirContext();
    doVarSubstitution(dirObject);

    BasicAttributes attrs = new BasicAttributes(true);
    Enumeration enum = dirObject.getAttributes();
   
    while(enum.hasMoreElements())
    {
      DirAttribute attr = (DirAttribute)enum.nextElement();

      if((!(attr.isBindName())) &&
         (!(attr.getName().equals("objectclass"))) &&
         (!(attr.isEmpty())))
      {
        BasicAttribute bAttr = new BasicAttribute(attr.getName());
        Vector values = attr.getValues();

        for(int i = 0; i < values.size(); i++)
        {
          bAttr.add(values.elementAt(i));
        }

        attrs.put(bAttr);
      }
    }

    int iModType = 0;

    if(modType.equals("replace"))
    {
      iModType = DirContext.REPLACE_ATTRIBUTE;
    }
    else if(modType.equals("add"))
    {
      iModType = DirContext.ADD_ATTRIBUTE;
    }
    else
    {
      iModType = DirContext.REMOVE_ATTRIBUTE;
    }

    dirContext.modifyAttributes(dirObject.getBindName(),iModType,attrs);
  }


  /**
   * Adds the given DirObject to the directory.
   *
   * @param  dirObject  DirObject to added to the directory.
   *
   * @throws  NamingException if the object could not be added to the 
   *          directory.
   */

  public void add(DirObject dirObject) throws NamingException
  {
    DirContext dirContext = m_jndiConfig.getDirContext();

    doVarSubstitution(dirObject);
    dirContext.bind(dirObject.getBindName(),dirObject); 
  }


  /**
   * Reads the DirObject with the given bind name from the directory and returns
   * it.  The bind name must be in the format "cn=foo" or "uid=blah" etc...
   *
   * @param  bindName   Bind name of DirObject to read.
   *
   * @return  DirObject with the given bind name from the directory.
   *
   * @throws  NamingException if the object could not be read.
   */

  public DirObject read(String bindName) throws NamingException
  {
    DirContext dirContext = m_jndiConfig.getDirContext();
    DirObject obj = (DirObject)dirContext.lookup(bindName); 

    return obj;
  }


  /**
   * Deletes the DirObject with the given bind name from the directory.
   * it.  The bind name must be in the format "cn=foo" or "uid=blah" etc...
   *
   * @param  bindName   Bind name of DirObject to read.
   *
   * @throws  NamingException if the object could not be deleted.
   */

  public void delete(String bindName) throws NamingException
  {
    DirContext dirContext = m_jndiConfig.getDirContext();
    dirContext.unbind(bindName); 
  }


  /**
   * Searches the current directory context using the given search filter and 
   * returns an Enumeration of matched DirObject instances.
   *
   * @param  subContext  Sub context to search in.
   * @param  filter      Search filter to use.
   * @param  returnAttrs Names of attributes to return.
   *
   * @return  Enumeration containing search results as DirObject instances.
   */

  public Enumeration search(String subContext,
                            String filter,
                            String returnAttrs[]) throws NamingException
  {
    Vector results = new Vector();
    DirContext dirContext = m_jndiConfig.getDirContext();

    SearchControls ctls = new SearchControls();
    ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    ctls.setReturningObjFlag(true);

    if(returnAttrs != null)
    {
      ctls.setReturningAttributes(returnAttrs);
    }
      
    NamingEnumeration ne = dirContext.search(subContext,filter,ctls);

    while(ne.hasMore())
    {
      try
      {
        SearchResult rs = (SearchResult)ne.next();
        results.add((DirObject)rs.getObject()); 
      }
      catch (ClassCastException cce)
      {
      }
    }

    ne.close();
    return results.elements();
  }

  
  /** 
   * Returns the DN of the given DirObject instance.
   *
   * @param  dirObject  DirObject instance.
   *
   * @return  String containing the DN of the given DirObject instance.
   */

  public String getDN(DirObject dirObject)
  {
    try
    {
      String ctxDn = m_jndiConfig.getDirContextName();
      String dirObjDn = dirObject.getBindName();

      return dirObjDn+","+ctxDn;
    }
    catch (NamingException ne)
    {
      ne.printStackTrace();
      return null;
    }
  }


  /**
   * Close the context.
   *
   * @throws  NamingException if the context cannot be closed.
   */

  public void close() throws NamingException 
  {
    m_jndiConfig.destroyDirContext();
  }


  //members
  private JNDIConfig m_jndiConfig = null;
}
