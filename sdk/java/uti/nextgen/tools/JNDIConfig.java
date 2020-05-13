/*
 * Id: $Id: JNDIConfig.java,v 1.4 2003/06/05 16:22:39 hannesh Exp $
 * 
 * $RCSfile: JNDIConfig.java,v $ $Revision: 1.4 $ $Date: 2003/06/05 16:22:39 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.tools;

import java.util.*;
import org.w3c.dom.*;
import javax.naming.*;
import javax.naming.directory.*;

/**
 * Encapsulates the JNDI naming context config specified in an XML document and 
 * provides access to the created naming or directory context.
 *
 * @author  Sanjeev Sharma
 */

public class JNDIConfig extends Object
{
  /**
   * Default constructor.
   */

  public JNDIConfig()
  {
  }


  /**
   * Creates a new JNDI config with the given XML Properties.
   *
   * @param  xmlProps  <code>XMLProperties</code> object containing a valid 
   *                   <i>JNDIConfig</i> element.
   * 
   * @throws  ConfException if an error occured while accessing the 
   *          configuration properties.
   */

  public JNDIConfig(XMLProperties xmlProps) throws ConfException
  {
    Element jndiElement = xmlProps.getElement("JNDIConfig");

    NamedNodeMap attrs = jndiElement.getAttributes();

    setType((attrs.getNamedItem("type")).getNodeValue());
    
    setFactoryInitial(xmlProps.getTextNodeValue(
                              xmlProps.getElement(jndiElement,
                                                  "FactoryInitial")));
    setProviderUrl(xmlProps.getTextNodeValue(
                              xmlProps.getElement(jndiElement,
                                                  "ProviderUrl")));
    setPrincipal(xmlProps.getTextNodeValue(
                              xmlProps.getElement(jndiElement,
                                                  "Principal")));
    setCredentials(xmlProps.getTextNodeValue(
                              xmlProps.getElement(jndiElement,
                                                  "Credentials")));
    setAuthentication(xmlProps.getTextNodeValue(
                              xmlProps.getElement(jndiElement,
                                                  "Authentication")));

    setSecurityProtocol(xmlProps.getTextNodeValue(
                              xmlProps.getElement(jndiElement,
                                                  "SecurityProtocol")));

    setObjectFactories(getPropertyList(xmlProps,
                                       jndiElement,
                                       "ObjectFactories",
                                       "ObjectFactory"));

    setStateFactories(getPropertyList(xmlProps,
                                      jndiElement,
                                      "StateFactories",
                                      "StateFactory"));
  } 


  /**
   * Returns a colon seperated list of properties.
   *
   * @param  xmlProps     XML configuration properties.
   * @param  jndiElement  Element containing the JNDI config.
   * @param  wrapperName  Name of the wrapper element.
   * @param  valueName    Name of the text node containing one of the parameter
   *                      values.  These are child nodes of the wrapper element.
   * 
   * @return  String containing the list of properties.
   */

  private String getPropertyList(XMLProperties xmlProps,
                                 Element jndiElement,
                                 String wrapperName, 
                                 String valueName) 
  {
    try
    {
      Element wrapperElement = xmlProps.getElement(jndiElement,wrapperName);
      NodeList nodeList = xmlProps.getElements(wrapperElement,valueName);

      StringBuffer buffer = new StringBuffer();

      for(int i = 0; i < nodeList.getLength(); i++)
      {
        buffer.append(xmlProps.getTextNodeValue(nodeList.item(i)));

        if (i < nodeList.getLength() - 1)
        {
          buffer.append(":");
        }
      }
    
      return buffer.toString();
    }
    catch (ConfException ce)
    {
      return null;
    }
  }
     

  /**
   * Sets the type of JNDIConfig, <i>file</i> or <i>ldap</i>.
   *
   * @param  type  New config type.
   */

  public void setType(String type)
  {
    m_type = type;
  }


  /**
   * Returns the type of this config object.
   *
   * @return  String specifying the type of this config object.
   */ 

  public String getType()
  {
    return m_type;
  }


  /**
   * Sets the name of the initial context factory used by this config object.
   *
   * @param   ctxFactory  Name of the initial context factory.
   */

  public void setFactoryInitial(String ctxFactory)
  {
    m_ctxFactory = ctxFactory;
  }


  /**
   * Returns the name fo the initial context factory used by this config object.
   *
   * @return  String containing the name of the intial context factory.
   */

  public String getFactoryInitial()
  {
    return m_ctxFactory;
  }


  /**
   * Sets the provider specific URL used by this config object to initialise the
   * initial context.
   *
   * @param  url  Provider specific URL.
   */

  public void setProviderUrl(String url)
  {
    m_url = url;
  }


  /**
   * Returns the provider specific URL used by this config object to initialise
   * the initial context.
   *
   * @return  String containing the provider specific URL.
   */

  public String getProviderUrl()
  {
    return m_url;
  }


  /**
   * Sets the security principal used by this config object to initialise the 
   * initial context.
   *
   * @param   principal  Security principal.
   */

  public void setPrincipal(String principal)
  {
    m_principal = principal;
  }


  /**
   * Returns the security principal used by this config object to initialise the
   * intial context.
   *
   * @return  String containing the value of the security principal.
   */

  public String getPrincipal()
  {
    return m_principal;
  }


  /**
   * Sets the security credentials used by this config object to intialise the
   * initial context.
   *
   * @param  credentials  Security credentials.
   */

  public void setCredentials(String credentials)
  {
    m_credentials = credentials;
  }


  /**
   * Returns the security credentials used by this config object to initialise
   * the intitial context.
   *
   * @return  String containing the security credentials.
   */

  public String getCredentials()
  {
    return m_credentials;
  }


  /**
   * Sets the security authentication used by this config object to initialise
   * the initial context.
   *
   * @param  authentication  Security authentication
   */

  public void setAuthentication(String authentication)
  {
    m_authentication = authentication;
  }


  /**
   * Returns the security authentication used by this config object to 
   * initialise the initial context.
   *
   * @return  String containing the security authentication.
   */

  public String getAuthentication()
  {
    return m_authentication;
  }


  /**
   * Sets the security protocol to be used by this configuration.
   *
   * @param  protocol  String indicating the security protocol to use e.g "ssl"
   */

  public void setSecurityProtocol(String protocol)
  {
    m_secProtocol = protocol;
  }


  /**
   * Returns the security protocol used by this ocnfiguration object.
   *
   * @return  String containing the security protocol.
   */

  public String getSecurityProtocol()
  {
    return m_secProtocol;
  }

 
  /**
   * Sets the value of the object factories property used by this config object.
   *
   * @param   objectFactories  Value of the object factories property.
   */

  public void setObjectFactories(String objectFactories)
  {
    m_objFactories = objectFactories;
  }


  /**
   * Returns the value of the object factories property.
   *
   * @return  String containing the value of the object factories property.
   */

  public String getObjectFactories()
  {
    return m_objFactories;
  }


  /**
   * Sets the value of the state factories property used by this config object.
   *
   * @param   stateFactories  Value of the state factories property.
   */

  public void setStateFactories(String stateFactories)
  {
    m_stateFactories = stateFactories;
  }


  /**
   * Returns the value of the state factories property.
   *
   * @return  String containing the value of the state factories property.
   */

  public String getStateFactories()
  {
    return m_stateFactories;
  }


  /**
   * Returns the hashtable containing all environment variables.
   *
   * @return  Hashtable containing naming environment variables.
   */

  private Hashtable getEnv()
  {
    Hashtable jndiEnv = new Hashtable();
    jndiEnv.put(Context.INITIAL_CONTEXT_FACTORY,getFactoryInitial());
    jndiEnv.put(Context.PROVIDER_URL,getProviderUrl());

    if(getStateFactories() != null)
    {
      jndiEnv.put(Context.STATE_FACTORIES,getStateFactories());
    }

    if(getObjectFactories() != null)
    {
      jndiEnv.put(Context.OBJECT_FACTORIES,getObjectFactories());
    }

    if(getType().equals("ldap"))
    {
      if(!(getSecurityProtocol().equals("none")))
      {
        jndiEnv.put(Context.SECURITY_PROTOCOL,getSecurityProtocol());
      }

      jndiEnv.put(Context.SECURITY_PRINCIPAL,getPrincipal());
      jndiEnv.put(Context.SECURITY_CREDENTIALS,getCredentials());
      jndiEnv.put(Context.SECURITY_AUTHENTICATION,getAuthentication());
    }

    return jndiEnv;
  }


  /**
   * Creates the initial context with the configuration encapsulated by this 
   * config object.
   *
   * @throws  NamingException if the JNDI context could not be created.
   */

  public void createContext() throws NamingException
  {
    m_context = new InitialContext(getEnv());
  }


  /**
   * Returns the name of the member context
   * 
   * @return  String containing the name of the member context.
   *
   * @throws  NamingException 
   */

  public String getContextName() throws NamingException
  {
    return m_context.getNameInNamespace();
  }


  /**
   * Returns the name of the member dir context
   *
   * @return  String containing the name of the member dir context.
   *
   * @throws  NamingException 
   */

  public String getDirContextName() throws NamingException
  {
    return m_dirContext.getNameInNamespace();
  }
  

  /**
   * Creates the initial dir context with the configuration encapsulated by 
   * this config object.
   *
   * @throws  NamingException if the JNDI dir context could not be created.
   */

  public void createDirContext() throws NamingException
  {
    m_dirContext = new InitialDirContext(getEnv());
  }


  /**
   * Returns the initial context created by this config object.
   *
   * @return  Context created by this config object.
   */

  public Context getContext()
  {
    return m_context;
  }

  
  /**
   * Returns the initial dir context created by this config object.
   *
   * @return DirContext created by this config object.
   */

  public DirContext getDirContext()
  {
    return m_dirContext;
  }


  /**
   * Destroy the member DirContext
   */

  public void destroyDirContext() throws NamingException
  {
    if(m_dirContext != null)
    {
      m_dirContext.close();
      m_dirContext = null;
    }
  } 


  /**
   * Destroy the member Context
   */

  public void destroyContext() throws NamingException
  {
    if(m_context != null)
    {
      m_context.close();
      m_context = null;
    }
  } 


  //members
  private String m_url = null;
  private String m_type = null;
  private Context m_context = null;
  private String m_principal = null;
  private String m_ctxFactory = null;
  private String m_credentials = null;
  private String m_secProtocol = null;
  private String m_objFactories = null;
  private String m_stateFactories = null;
  private String m_authentication = null;
  private DirContext m_dirContext = null;
}
