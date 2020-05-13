/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.ds.web;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import uti.nextgen.ds.*;
import uti.nextgen.tools.*;
import uti.nextgen.soapclient.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Controlling servlet for dgServices web components.
 * <p>
 * The servlet functions as a SOAP client to the dgServices SOAP service. The
 * servlet manages all interactions with the web service.  Display operations
 * are handled by JSP's
 * <p>
 * Requests to this servlet take the following form: 
 *  http://server/dgservices/dsservlet?action=<action>&userid=<userid>
 * <p>
 * The following actions are supported:
 * <p>
 * <li>chpword</li>
 * <li>pword</li>
 * <li>menu</li>
 * <li>grpsearch</li>
 * <li>grpalloc</li>
 * <li>grpdealloc</li>
 * <p>
 *
 * @author  Sanjeev Sharma
 */

public class DsServlet extends HttpServlet
{
  /**
   * Initialise the DsServlet. 
   *
   * @param  config  Servlet configuration properties.
   *
   * @throws  ServletException
   */

  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
    ServletContext context = getServletContext();

    try
    {
      XMLProperties xmlProps = new XMLProperties(
                       context.getRealPath(config.getInitParameter("Config")));

      m_operationPool = new DsSOAPOperationPool();
      m_operationPool.init("dssoapop",xmlProps);

      m_userSuffix = config.getInitParameter("UserSuffix");
      m_noSuffixDn = config.getInitParameter("NoSuffixDn");

      m_jspTemplates = new Hashtable();
      m_messages = new Hashtable();

      NodeList opList = xmlProps.getElements(xmlProps.getElement("Operations"),
                                             "Operation");

      for(int i = 0; i < opList.getLength(); i++)
      {
        Element opElement = (Element)opList.item(i);

        String name = xmlProps.getTextNodeValue(opElement,"name");
        String msg = xmlProps.getTextNodeValue(opElement,"message");
        String tmpl = xmlProps.getTextNodeValue(opElement,"template");

        m_jspTemplates.put(name,tmpl);
        m_messages.put(name,msg);
      }
    }
    catch (Exception e)
    {
      throw new ServletException(e);
    }
  }


  /**
   * Handles all HTTP GET requests.
   *
   * @param  request   GET request.
   * @param  response  Response channel.
   *
   * @throws ServletException 
   * @throws IOException
   */

  public void doGet(HttpServletRequest request,
                    HttpServletResponse response) throws ServletException,
                                                         IOException
  {
    try
    {
      String action = request.getParameter("action");
      String password = request.getParameter("password");
      String uid = request.getParameter("userid");
      String jspTemplate = (String)m_jspTemplates.get(action);
      String message = (String)m_messages.get(action);
      HttpSession session = request.getSession(true);

      if(password == null)
      {
        Object obj = session.getAttribute("password");

        if(obj != null)
        {
          password = (String)obj;
        }
        else
        {
          password = "dummy";
        }
      }

      log("HTTP GET Received action: "+action+" and uid: "+uid);

      DsSOAPOperation operation = getOperationInstance(uid,password);

      request.setAttribute("message",message);

      if(action.equals("grpsearch"))
      {
        request.setAttribute("GroupList",getGroupList(operation));
      }
      else if((action.equals("grpalloc")) || (action.equals("grpdealloc")))
      {
        setGroupModAttributes(request,operation);
      }

      RequestDispatcher dispatcher = 
                          getServletContext().getRequestDispatcher(jspTemplate);
      dispatcher.forward(request,response);

      m_operationPool.releaseResource(operation);
    }
    catch (Exception e)
    {
      throw new ServletException(e);
    }
  }


  /**
   * Handles all HTTP POST requests.
   *
   * @param  request   GET request.
   * @param  response  Response channel.
   *
   * @throws ServletException 
   * @throws IOException
   */

  public void doPost(HttpServletRequest request,
                     HttpServletResponse response) throws ServletException,
                                                          IOException
  {
    try
    {
      String message = null;
      String action = request.getParameter("action");
      String password = request.getParameter("password");
      String uid = request.getParameter("userid");
      String jspTemplate = (String)m_jspTemplates.get(action);
      HttpSession session = request.getSession(false);

      if(password == null)
      {
        password = (String)session.getAttribute("password");
      }

      log("HTTP POST Received action: "+action+" and uid: "+uid);

      DsSOAPOperation operation = getOperationInstance(uid,password);

      if(action.equals("chpword"))
      {
        message =  changePassword(operation,uid,
                                  (String)request.getParameter("newpassword"));
      }
      else if(action.equals("pword"))
      {
        message = bind(operation,uid);
 
        if(message.equals("success"))
        {
          session.setAttribute("password",password);
          jspTemplate = (String)m_jspTemplates.get("menu");
          message = (String)m_messages.get("menu");
        }
      }
      else if((action.equals("grpalloc")) || (action.equals("grpdealloc")))
      {
        message = modifyGroup(action,request,operation);
      }

      request.setAttribute("message",message);
      RequestDispatcher dispatcher = 
                        getServletContext().getRequestDispatcher(jspTemplate);
      dispatcher.forward(request,response);
      
      m_operationPool.releaseResource(operation);
    }
    catch (Exception e)
    {
      throw new ServletException(e);
    }
  }


  /**
   * Returns a DsSOAPOperation instance for use in a request.
   *
   *
   * @param  uid       Id of user executing request.
   * @param  password  Password of user executing request.
   *
   * @return  DsSOAPOperation instance.
   *
   * @throws  ResourcePoolException if an operation instance could not be
   *          obtained.
   */

  private DsSOAPOperation getOperationInstance(String uid, 
                                               String password) 
                                                   throws ResourcePoolException
  {
    DsSOAPOperation operation = (DsSOAPOperation)m_operationPool.getResource();

    if(uid.equals(m_noSuffixDn))
    {
      operation.setBindDN(m_noSuffixDn);
    }
    else
    {
      operation.setBindDN("uid="+uid+","+m_userSuffix);         
    }

    operation.setPassword(password);

    return operation;
  }


  /**
   * Set all request attributes for  group modifications.
   *
   * @param  request    Received request.
   * @param  operation  Operation instance to use.
   *
   * @throw  ServletException
   */

  public void setGroupModAttributes(HttpServletRequest request,
                                    DsSOAPOperation operation) 
                                                        throws ServletException
  {
    try
    {
      DsMessage userList[] = null;
      String uidFilter = request.getParameter("uidFilter");
      String cnFilter = request.getParameter("cnFilter");
      String groupName = request.getParameter("groupName"); 

      if(uidFilter.length() > 0)
      {
        userList = getUsers("uid",uidFilter,operation);
      }
      else if(cnFilter.length() > 0)
      {
        userList = getUsers("cn",cnFilter,operation);
      }
      
      request.setAttribute("UserList",
                           userList != null ? userList : new DsMessage[] { });
      request.setAttribute("GroupList",getGroupList(operation));
      request.setAttribute("ModGroup",getGroup(groupName,operation));
    }
    catch (SOAPClientException spe)
    {
      throw new ServletException(spe);
    }
  }


  /**
   * Returns the group with the given common name.
   *
   * @param  cn         Common name of group.
   * @param  operation  Operation instance to use.
   *
   * @return  DsMessgae containing group information.
   *
   * @throws  SOAPClientException
   */

  private DsMessage getGroup(String cn, 
                             DsSOAPOperation operation) 
                                               throws SOAPClientException
  {
    DsMessage msg = new DsMessage();
    msg.setType("search");
    msg.setSubtype("none");
    msg.setObjectType("group");

    msg.addAttribute("cn",cn);
    
    log("getGroup doing SOAPCall");
    DsMessage response = (operation.doOperation(msg))[0];
    log("getGroup finished SOAPCall");
   
    return response; 
  }


  /**
   * Returns the users produced by a search with the given filter.
   * 
   * @param  filterType   Type of filter: "uid" or "cn".
   * @param  filterValue  Use this value to search directory service.
   * @param  operation    Operation instance to use.
   *
   * @return  DsMessage[] containing user information.
   *
   * @throws  SOAPClientException
   */

  private DsMessage[] getUsers(String filterType, 
                               String filterValue,
                               DsSOAPOperation operation) 
                                                 throws SOAPClientException
  {
    DsMessage msg = new DsMessage();
    msg.setType("search");
    msg.setSubtype("none");
    msg.setObjectType("person");

    msg.addAttribute(filterType,filterValue);
    
    log("getUsers doing SOAPCall");
    DsMessage response[] = operation.doOperation(msg);
    log("getUsers finished SOAPCall");
   
    return response; 
  }


  /**
   * Change password for currently logged in user.
   *
   * @param  operation  DsSOAPOperation instance to use.
   * @param  uid        User id of currently logged in user.
   * @param  newPass    New password.
   *
   * @throws SOAPClientException if an exception occurs while invoking the
   *         Directory Service.
   */

  public String changePassword(DsSOAPOperation operation,
                               String uid,
                               String newPass) throws SOAPClientException
  {
    DsMessage msg = new DsMessage();
    msg.setType("modify");
    msg.setSubtype("replace");
    msg.setObjectType("person");

    msg.addAttribute("uid",uid);
    msg.addAttribute("userpassword",newPass);

    log("changePassword doing SOAPCall");
    DsMessage[] response = operation.doOperation(msg);
    log("changePassword finished SOAPCall");

    String error = (String)(response[0].getAttributeValue("error"));

    return error;
  }


  /**
   * Tests to see if the operation credentials are ok. 
   *
   * @param  operation  DsSOAPOperation instance to use.
   * @param  uid        User id of currently logged in user.
   *
   * @throws SOAPClientException if an exception occurs while invoking the
   *         Directory Service.
   */

  public String bind(DsSOAPOperation operation,
                      String uid) throws SOAPClientException
  {
    DsMessage msg = new DsMessage();
    msg.setType("search");
    msg.setSubtype("none");
    msg.setObjectType("person");
    
    msg.addReturnAttributeName("cn");
    msg.addReturnAttributeName("uid");

    msg.addAttribute("uid",uid);

    log("bind doing SOAPCall");
    DsMessage[] response = operation.doOperation(msg);
    log("bind finished SOAPCall");

    Object error = null;

    if(response != null)
    {
      error = (response[0].getAttributeValue("error"));
    }

    if(error != null)
    {
      return (String)error;
    }
    else
    {
      return new String("success");
    }
  }


  /**
   * Returns a DsMessage array containing all the common names and distinguished
   * names of all LDAP groups.
   * 
   * @param  operation  DsSOAPOperation instance to use.
   *
   * @return  DsMessage[] containing group information.
   *
   * @throws  SOAPClientException if the web service cannot be contacted.
   */

  private DsMessage[] getGroupList(DsSOAPOperation operation) 
                                                     throws SOAPClientException
  {
    DsMessage msg = new DsMessage();
    msg.setType("search");
    msg.setSubtype("none");
    msg.setObjectType("group");

    msg.addReturnAttributeName("cn");

    msg.addAttribute("cn","*");
    
    log("getGroupList doing SOAPCall");
    DsMessage[] response = operation.doOperation(msg);
    log("getGroupList finished SOAPCall");
   
    return response; 
  }


  /**
   * Modifies a specific LDAP group with the parameters provided in the given
   * request. 
   *
   * @param  task       Group modification task to execute.
   * @param  request    Received request.
   * @param  operation  Operation instance to use.
   * 
   * @return  String indicating the success or failure of the operation.
   * @throws SOAPClientException
   */

  private String modifyGroup(String task, 
                             HttpServletRequest request, 
                             DsSOAPOperation operation) 
                                                   throws SOAPClientException
  {
    DsMessage msg = new DsMessage();
    msg.setType("modify");
    msg.setObjectType("group");

    StringTokenizer tk = null;

    if(task.equals("grpalloc"))
    {
      msg.setSubtype("add");
      tk = new StringTokenizer(request.getParameter("addedRight"),"|");
    }
    else
    {
      msg.setSubtype("remove");
      tk = new StringTokenizer(request.getParameter("addedLeft"),"|");
    }

    String groupName = request.getParameter("groupName");
    msg.addAttribute("cn",groupName);

    Vector members = new Vector(); 

    while(tk.hasMoreTokens())
    {
      String token = tk.nextToken();
      log("Scheduling '"+token+"' to be added to '"+groupName+"'");
      members.add(token);
    }

    msg.addAttribute("uniquemember",members);
    
    log("modifyGroup doing SOAPCall");
    DsMessage[] response = operation.doOperation(msg);
    log("modifyGroup finished SOAPCall");

    Object error = null;

    if(response != null)
    {
      error = (response[0].getAttributeValue("error"));
    }

    return (String)error; 
  }
 

  //members
  private String m_userSuffix = null;
  private String m_noSuffixDn = null;
  private Hashtable m_messages = null;
  private Hashtable m_jspTemplates = null;
  private DsSOAPOperationPool m_operationPool = null;
}
