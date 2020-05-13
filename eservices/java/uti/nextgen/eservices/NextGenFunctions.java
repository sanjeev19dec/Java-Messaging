/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.eservices;

import java.util.*;
import org.w3c.dom.*;
import uti.nextgen.tools.*;

/**
 * Container that manages all the Function objects defined in the API
 * configuration file.
 * <p>
 * The class maintains a hashtable of functions. The functions can be accessed
 * by name using the getFuction(..) method or by the type of request it
 * processes by using the getFunctionByRequestType(..) method.
 *
 * @author  Sanjeev Sharma
 */

public class NextGenFunctions extends Object
{
  /**
   * Initialises the functions wrapper with the name of the XML configuration
   * file.
   * <p>
   * Initialises the database configuration and all function objects defined in
   * the XML configuration.
   */

  public NextGenFunctions()
  {
    try
    {
      m_functionTable = new Hashtable();
      m_requestTypeTable = new Hashtable();

      XMLProperties xmlProps = NextGenEnv.getConfig();
      m_jdbcConf = NextGenEnv.getJDBCConfig();

      NodeList functionList = xmlProps.getElements(
                                             xmlProps.getElement("Functions"),
                                            "Function");

      for(int i = 0; i < functionList.getLength(); i++)
      {
        Element funcElement = (Element)functionList.item(i);

        String name = xmlProps.getAttributeValue(funcElement,"name");
        String className = xmlProps.getAttributeValue(funcElement,"class");
        String requestType =
                    xmlProps.getAttributeValue(funcElement,"requestType");

        //Parse request type for multiple requests
        String[] strReqType = parseRequestType(requestType);

        Function function =
                       (Function)((Class.forName(className)).newInstance());
        function.init(name,xmlProps,m_jdbcConf);

        m_functionTable.put(name,function);

        for(int j = 0; j < strReqType.length; j++)
        {
          m_requestTypeTable.put(strReqType[j].toLowerCase(),name);
        }
      }
    }
    catch (ConfException ce)
    {
      ce.printStackTrace();
    }
    catch (ClassNotFoundException cnfe)
    {
      cnfe.printStackTrace();
    }
    catch (InstantiationException ie)
    {
      ie.printStackTrace();
    }
    catch (IllegalAccessException ae)
    {
      ae.printStackTrace();
    }
  }

  /**
   * This function takes a comma delimited string and creates an array of
   * strings containing the individual items
   *
   * @param   valueString    The string to parse.
   * @return  an array of strings parsed from the input string.
   */

   private String[] parseRequestType(String reqType)
   {
     StringTokenizer tokens = new StringTokenizer(reqType, ",");
     int iNo = tokens.countTokens();
     String reqTypeValues[] = new String[iNo];

     int i=0;

     while (tokens.hasMoreTokens())
     {
       reqTypeValues[i++] = tokens.nextToken().trim();
     }

     return reqTypeValues;
   }


  /**
   * Returns the named function object instance.
   *
   * @param  name  Name of function to return.  This parameter must correspond
   *               with one of the name attribute values in the API
   *               configuration file.
   *
   * @return  Function instance.
   */

  public Function getFunction(String name)
  {
    return (Function)m_functionTable.get(name);
  }


  /**
   * Returns the appropriate function base on the request type it supports.
   *
   * @param  requestType  Given request type e.g "input", "tree", etc..
   *
   * @return  Function that supports the given request type.
   */

  public Function getFunctionByRequestType(String requestType)
  {
    String name = (String)m_requestTypeTable.get(requestType);

    return (Function)m_functionTable.get(name);
  }


  /**
   * Destroys the function container by destroying the database configuration
   */

  public void destroy()
  {
    m_requestTypeTable = null;
    m_functionTable = null;
  }


  //members
  private JDBCConfig m_jdbcConf = null;
  private Hashtable m_functionTable = null;
  private Hashtable m_requestTypeTable = null;
}
