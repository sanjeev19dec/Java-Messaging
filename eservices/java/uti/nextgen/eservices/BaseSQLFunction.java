/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.eservices;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.w3c.dom.*;
import java.util.logging.*;
import uti.nextgen.tools.*;
import uti.nextgen.eservices.util.*;

/**
 * Function that provides base functionality to do dynamic SQL functions.
 *
 * @author  Sanjeev Sharma
 */

public abstract class BaseSQLFunction implements Function
{
  /**
   * Initialises the XML template document, base SQL selects and allowed fields
   * LinkedList.
   *
   * @param  name      Function name
   * @param  xmlProps  XML configuration properties.
   * @param  jdbcConf  Database configuration.
   */

  public void init(String name, XMLProperties xmlProps, JDBCConfig jdbcConf)
  {

    try
    {
      m_dom = new DomImpl();
      m_jdbcResourcePool = NextGenEnv.getResourcePool();

      Element config = xmlProps.getElementByNameAttr("Function",name);

      NodeList nodeList = xmlProps.getElements(
                             xmlProps.getElement(config,"AllowedFields"),
                             "Field");

      m_allowedList = new LinkedList();

      for(int i = 0; i < nodeList.getLength(); i++)
      {
        m_allowedList.add(xmlProps.getTextNodeValue(nodeList.item(i)));
      }

      String sqlConf = xmlProps.getTextNodeValue(config,"SQLFile");
      m_maxRows  = Integer.parseInt(
                         xmlProps.getTextNodeValue(config,"MaxResultSize"));

      XMLProperties sqlProps = new XMLProperties(sqlConf);

      m_responseSelect =
           sqlProps.getTextNodeValue(sqlProps.getElement("ResponseSelect"));

      m_headerSelect =
           sqlProps.getTextNodeValue(sqlProps.getElement("HeaderSelect"));

      m_titleSelect =
           sqlProps.getTextNodeValue(sqlProps.getElement("TitleSelect"));

      m_fieldSelect =
           sqlProps.getTextNodeValue(sqlProps.getElement("FieldSelect"));

      m_singleFieldSelect =
           sqlProps.getTextNodeValue(sqlProps.getElement("SingleFieldSelect"));

      m_drilldownIdSelect =
           sqlProps.getTextNodeValue(sqlProps.getElement("DrilldownIdSelect"));

      m_drilldownFieldSelect =
         sqlProps.getTextNodeValue(sqlProps.getElement("DrilldownFieldSelect"));

      m_pageLengthSelect =
           sqlProps.getTextNodeValue(sqlProps.getElement("PageLengthSelect"));

      m_templateName = xmlProps.getTextNodeValue(config,"XMLTemplate");

      sqlProps = null;
    }
    catch (ConfException ce)
    {
      m_logger.log(Level.SEVERE,"Unable to initialise: "+ce.getMessage(),ce);
    }
    catch (IOException ioe)
    {
      m_logger.log(Level.SEVERE,"Unable to initialise: "+ioe.getMessage(),ioe);
    }
  }


  /**
   * This method must contain the implementation code to build up the SQL
   * required to perform the query.
   *
   * @param  doc  Document containing parameter data to be used in the query.
   *
   * @return  String containing SQL query to execute.
   */

  protected abstract String buildQuery(Document doc);


  /**
   * Checks to see if the given name value pair extracted from a urequest
   * message is valid for processing.  Any empty or null value will result
   * in a false result.
   *
   * @param  name  Field id or code.
   * @param  val   Field value.
   *
   * @return  boolean indicating the validity.
   */

  public boolean isValuePairValid(String name, String value)
  {
    if((name == null) || (value == null) ||
       ((name.trim()).length() == 0) ||
       ((value.trim()).length() == 0))
    {
      return false;
    }
    else
    {
      return true;
    }
  }


  /**
   * Checks to see if the given NodeList contains a parameter element containing
   * a field_id element with a specific value.
   *
   * @param  nodeList    NodeList of parameter elements.
   * @param  Val  Value of field_id element.
   *
   * @return boolean indicating that the field_id exists within the given
   &                 NodeList.
   */

  public boolean hasParameter(NodeList nodeList,String fieldIdVal)
  {
    for(int i = 0; i < nodeList.getLength(); i++)
    {
      Node idNode = m_dom.getElement("field_cd",(Element)nodeList.item(i));
      String idNodeVal = m_dom.getTextNodeValue(idNode);

      if(idNodeVal == null)
      {
        continue;
      }
      else if(idNodeVal.equals(fieldIdVal))
      {
        return true;
      }
    }

    return false;
  }


  /**
   * Checks to see if the given field name is allowed in the where clause of
   * the database query.  Comparison is done against the entries in the
   * allowed fields LinkedList.
   *
   * @param  fieldName  In the form schema.table.column.
   *
   * @return  boolean to indicate whether or not the given field name is allowed
   *          in the query's where clause.
   */

  public boolean isFieldAllowed(String fieldName)
  {
    fieldName = fieldName.toLowerCase();
    ListIterator iter = m_allowedList.listIterator();

    while(iter.hasNext())
    {
      Object obj = iter.next();

      if(obj != null)
      {
        if(fieldName.equals(((String)obj).toLowerCase()))
        {
          return true;
        }
      }
    }

    return false;
  }


  /**
   * Checks to see if the given page is the last page
   * in the hitlist.
   *
   * @param  pLen  Length of the page.
   * @param  PNum  Number of the page.
   *
   * @return  boolean to indicate whether or not the given page is last page
   */

  public boolean isLastPage(int pLen, int pNum)
  {
    int lastpage = 0;

    if ((totalCount % pLen) == 0)
    {
      lastpage = totalCount/pLen;
    }
    else
    {
      lastpage = (totalCount/pLen) + 1;

      if(lastpage == pNum)
      {
        return true;
      }
    }

    return false;
  }


  /**
   * Selects field information from the database and returns a Field instance
   * containing the information.  This method is used to obtain field names from
   * the information supplied in a urequest message.
   *
   * @param  fieldId  The key that must be used to perform the select.
   *
   * @throws  SQLException
   */

  public Field getField(String sfieldCd) throws SQLException
  {
    Field field = null;
    Connection conn = null;

    try
    {
      conn = (Connection)m_jdbcResourcePool.getResource();
    }
    catch (ResourcePoolException rpe)
    {
      throw new SQLException(
          "Unable to retrieve connection from ResourcePool: "+rpe.getMessage());
    }

    PreparedStatement statement = conn.prepareStatement(m_singleFieldSelect);
    statement.setString(1,sfieldCd);

    ResultSet rs = statement.executeQuery();

    if(rs.next())
    {
      String strFieldId = rs.getString("field_id");
      Integer fieldIdInt = new Integer(strFieldId);
      String name = rs.getString("display_name");
      String schema = rs.getString("owner");
      String table = rs.getString("table_name");
      String column = rs.getString("column_name");
      String type = rs.getString("data_type");
      Integer length = new Integer(rs.getInt("data_length"));
      Integer precision = new Integer(rs.getInt("data_precision"));
      String fieldCd = rs.getString("field_cd");
      java.sql.Date timeStamp = rs.getDate("timestamp");
      String deleteInd = rs.getString("delete_ind");
      String funcCall = rs.getString("db_function_call");
      String funcAlias = rs.getString("db_function_alias");
      String globalInd = rs.getString("global_local_ind");

      field =
          new Field(0,fieldIdInt,name,schema,table,column,type,length,precision,
                     fieldCd,timeStamp,deleteInd,funcCall,funcAlias,globalInd);
    }

    rs.close();
    statement.close();

    try
    {
      m_jdbcResourcePool.releaseResource(conn);
    }
    catch (ResourcePoolException rpe)
    {
      rpe.printStackTrace();
    }

    return field;
  }


  /**
   * Convenience method to create initial select-from part of the query that
   * must be used to query the database.
   *
   * @param  buffer  StringBuffer to append to.
   */

  public void createInitialFrom(StringBuffer buffer)
  {
    buffer.append("select ");
    appendFromLinkedList(m_fieldManager.getFullNames(),buffer);

    buffer.append(" from ");
    appendFromLinkedList(m_fieldManager.getFullTableNames(),buffer);
  }


  /**
   * Appends the contents of the given LinkedList to the given StringBuffer.
   *
   * @param  list    LinkedList containing data to append.
   * @param  buffer  StringBuffer to append to.
   */

  private void appendFromLinkedList(LinkedList list,StringBuffer buffer)
  {
    for(int i = 0; i < list.size(); i++)
    {
      if(i < list.size() - 1)
      {
        buffer.append((String)list.get(i)+",");
      }
      else
      {
        buffer.append((String)list.get(i));
      }
    }
  }


  /**
   * Extract and return the Page No.
   *
   * @param
   *
   * @return  int Page No from the uRequest
   *
   * @throws
   */

  private int getPageNumber(Document doc)
  {
    int pageNumber = 0;
    //Determine the page number
    NodeList nav = doc.getElementsByTagName("navigation");

    Node pNode = m_dom.getElement("page_no",(Element)nav.item(0));
    Integer pageNo = new Integer(m_dom.getTextNodeValue(pNode));

    pageNumber = pageNo.intValue();

    if (pageNumber == 0)
    {
      pageNumber = 1;
    }

    return pageNumber;
  }


  /**
   * Extract and return the default page length for a specific function.
   *
   * @param  functionId  Function id to use when doing the page length select
   * @param  conn        Database connection
   *
   * @return  int containing the default page length for the specific function
   *              or the configured default.
   *
   * @throws SQLException if an error occurs while accessing the database.
   */

  private int getPageLength(Integer functionId,
                            Connection conn) throws SQLException
  {
    int pageLen = 0;
    PreparedStatement statement = conn.prepareStatement(m_pageLengthSelect);

    statement.setObject(1,functionId);

    ResultSet rs = statement.executeQuery();

    if(rs.next())
    {
      pageLen = rs.getInt(1);

      if(pageLen == 0)
      {
        pageLen = m_maxRows;
      }
    }

    rs.close();
    statement.close();

    m_logger.log(Level.INFO,"Page length for function_id: "+functionId+" is: "+
                 pageLen);

    return pageLen;
  }


  /**
   * Returns a clone of the uresponse element.
   *
   * @return Node
   */

  private Node getUresponseClone()
  {
    Node uresponse = m_dom.getElement("uresponse",m_template);
    return uresponse.cloneNode(true);
  }

  /**
   * Returns a clone of the uresponse element.
   *
   * @return Node
   */

  private Node getNavigationClone()
  {
    Node navigate = m_dom.getElement("navigation",m_template);
    return navigate;
  }

  /**
   * Returns a clone of the row element.
   *
   * @return Node
   */

  private Node getRowClone()
  {
    Node row = m_dom.getElement("row",m_template);
    return row.cloneNode(true);
  }


  /**
   * Returns a clone of the col element.
   *
   * @return Node
   */

  private Node getColClone()
  {
    Node col = m_dom.getElement("col",m_template);
    return col.cloneNode(true);
  }


  /**
   * Returns a clone of the header element.
   *
   * @return Node
   */

  private Node getHeaderClone()
  {
    Node header = m_dom.getElement("header",m_template);
    return header.cloneNode(true);
  }


  /**
   * Intialises a hash map of Field instances for use when constructing
   * queries.  The hash map stores field id's as keys and Field instances as
   * values.
   *
   * @param  functionId  Parameter to use in query for fields.
   * @param  conn        Database connection
   *
   * @throws  SQLException
   */

  private void initFields(Integer functionId,
                          Connection conn) throws SQLException
  {
    int count = 0;
    PreparedStatement statement = conn.prepareStatement(m_fieldSelect);

    statement.setObject(1,functionId);
    ResultSet rs = statement.executeQuery();

    LinkedHashMap fieldMap = new LinkedHashMap();

    StringBuffer buf = new StringBuffer();
    buf.append("Fields configured for function_id '"+functionId+"'\n");

    while(rs.next())
    {
      String strFieldId = rs.getString("field_id");
      Integer fieldIdInt = new Integer(strFieldId);
      String name = rs.getString("display_name");
      String schema = rs.getString("owner");
      String table = rs.getString("table_name");
      String column = rs.getString("column_name");
      String type = rs.getString("data_type");
      Integer length = new Integer(rs.getInt("data_length"));
      Integer precision = new Integer(rs.getInt("data_precision"));
      String fieldCd = rs.getString("field_cd");
      java.sql.Date timeStamp = rs.getDate("timestamp");
      String deleteInd = rs.getString("delete_ind");
      String funcCall = rs.getString("db_function_call");
      String funcAlias = rs.getString("db_function_alias");
      String globalInd = rs.getString("global_local_ind");

      Field field =
          new Field(count++,fieldIdInt,name,schema,table,column,type,length,
                    precision,fieldCd,timeStamp,deleteInd,funcCall,funcAlias,
                    globalInd);

      fieldMap.put(strFieldId,field);
      buf.append("    "+field.getFullName()+"\n");
    }

    rs.close();
    statement.close();

    m_logger.log(Level.FINE,buf.toString());
    buf = null;

    m_fieldManager = new FieldManager(fieldMap);
  }


  /**
   * Creates all the necessary responseType elements.
   *
   * @param  requestType
   * @param  profileId
   * @param  participantId
   *
   * @throws SQLException
   */

  private void createResponseType(String requestType,
                                  Integer profileId,
                                  Integer participantId) throws SQLException
  {
    //String RequestType = String.valueOf(reqId);
    Node uresponseRt = m_dom.getElement("uresponse",m_template);

    Element uresponse = (Element)uresponseRt;

    Node respType = m_dom.getElement("resp_type",uresponse);
    respType.appendChild(m_template.createTextNode(requestType));
  }


  /**
   * Creates all the necessary header elements.
   *
   * @param  functionId  Parameter to use in query for headers.
   *
   * @throws SQLException
   */

  private void createNavigation(int pageLen, int pageNo) throws SQLException
  {
    Node hitlist = m_dom.getElement("hitlist",m_template);

    String pagenum = String.valueOf(pageNo);
    String pageLength = String.valueOf(pageLen);
    String totalRecords = String.valueOf(totalCount);

    //Element nav = (Element)hitlist;
    Element nav = (Element)getNavigationClone();

    // Populating page number
    Node page = m_dom.getElement("page_no",nav);
    page.appendChild(m_template.createTextNode(pagenum));

    // Populating if its first page
    Node fpage = m_dom.getElement("page_length",nav);
    fpage.appendChild(m_template.createTextNode(pageLength));

    //Populating total count in record set in last page
    Node lpage = m_dom.getElement("records_found",nav);
    lpage.appendChild(m_template.createTextNode(totalRecords));
  }


  /**
   * Sets the title for this hitlist.
   *
   * @param  funcionId  Current function begin executed.
   * @param  conn       Database connection.
   *
   * @throws  SQLException
   */

  private void setTitle(Integer functionId, Connection conn) throws SQLException
  {
    PreparedStatement stmnt = conn.prepareStatement(m_titleSelect);
    stmnt.setObject(1,functionId);

    ResultSet rs = stmnt.executeQuery();

    if(rs.next())
    {
      String title = rs.getString("display_heading");

      if(title != null)
      {
        Element hitlistElement = (Element)m_dom.getElement("hitlist",
                                                           m_template);
        Node titleElement = m_dom.getElement("title",hitlistElement);
        titleElement.appendChild(m_template.createTextNode(title));
      }
    }

    rs.close();
    stmnt.close();
  }


  /**
   * Creates all the necessary header elements.
   *
   * @param  functionId  Parameter to use in query for headers.
   * @param  conn        Database connection.
   * @param  hiddenFields  Vector containing hidden field indexes.
   *
   * @throws SQLException
   */

  private void createHeaders(Integer functionId,Integer profileId,Integer participantId,
                             Connection conn,
                             Vector hiddenFields) throws SQLException
  {
    Node hitlist = m_dom.getElement("hitlist",m_template);

    PreparedStatement statement = conn.prepareStatement(m_headerSelect);

    statement.setObject(1,functionId);
	statement.setObject(2,profileId);
	statement.setObject(3,participantId);

	statement.setObject(4,functionId);
	statement.setObject(5,profileId);
	statement.setObject(6,participantId);

    ResultSet rs = statement.executeQuery();
    Node row = m_dom.getElement("row",m_template);

    String rsVal = null;
    int count = 0;
    int disWidth = 0;

    while(rs.next())
    {
      count++;
      rsVal = rs.getString("display_type");

      if((rsVal != null) && (rsVal.equals("HIDDEN")))
      {
        hiddenFields.add(new Integer(count));
      }
      else
      {
        Element header = (Element)getHeaderClone();

        if(rsVal != null)
        {
          Node type = m_dom.getElement("disp_type",header);
          type.appendChild(m_template.createTextNode(rsVal));
        }

        rsVal = rs.getString("display_heading");

        if(rsVal != null)
        {
          Node heading = m_dom.getElement("disp_heading",header);
          heading.appendChild(m_template.createTextNode(rsVal));
        }

        disWidth = rs.getInt("display_width");

        if(new Integer(disWidth) != null && disWidth !=0)
        {
          Node width = m_dom.getElement("disp_width",header);
          width.appendChild(m_template.createTextNode(Integer.toString(disWidth)));
        }
        else
        {
		  Node width = m_dom.getElement("disp_width",header);
		  header.removeChild(width);
		}

        hitlist.insertBefore(header,row);
      }
    }

    Node origHeader = m_dom.getElement("header",m_template);
    hitlist.removeChild(origHeader);

    rs.close();
    statement.close();
  }


  /**
   * Creates a vector of fields that will be used to create a drilldown URL for
   * the given field.
   *
   * @param  funcionId     Current function id.
   * @param  field         Field to create a drilldown URL for.
   * @param  conn          Database connection
   *
   * @return  Vector of Field instances.
   */

  private Vector getUrlFields(Integer functionId,
                              Field field,
                              Connection conn) throws SQLException
  {
    Integer ddId = null;
    Vector fields = new Vector();

    PreparedStatement ddIdStmnt = conn.prepareStatement(m_drilldownIdSelect);

    ddIdStmnt.setObject(1,functionId);
    ddIdStmnt.setObject(2,field.getFieldId());

    ResultSet ddIdRs = ddIdStmnt.executeQuery();

    if(ddIdRs.next())
    {
      String id = ddIdRs.getString("drilldown_service_id");

      if(id != null)
      {
        ddId = new Integer(id);
      }
    }

    ddIdRs.close();
    ddIdStmnt.close();

    if(ddId != null)
    {
      PreparedStatement ddFieldStmnt =
                             conn.prepareStatement(m_drilldownFieldSelect);
      ddFieldStmnt.setObject(1,ddId);

      ResultSet ddFieldRs = ddFieldStmnt.executeQuery();

      while(ddFieldRs.next())
      {
        fields.add(m_fieldManager.getField(ddFieldRs.getString("field_id")));
      }

      ddFieldRs.close();
      ddFieldStmnt.close();
    }

    return fields;
  }

  /**
   * Creates all the required Row elements based on the outcome of the given
   * query.
   *
   * @param  query         SQL query to execute.
   * @param  pageLen       Maximum number of rows to return from the query.
   * @param  functionId    Current function being executed.
   * @param  conn          Database connection
   * @param  hiddenFields  Vector containing hidden field indexes.
   *
   * @throws SQLException
   */

  private void createRows(String query,
                          int pageLen,
                          int pageNo,
                          Integer functionId,
                          Connection conn,
                          Vector hiddenFields) throws SQLException
  {
    try
    {
      Connection urlConn = (Connection)m_jdbcResourcePool.getResource();

      int rowCursor = 0; //Counter to determine cursor position
      int imax = 0; //Counter for page Length

      Node hitlist = m_dom.getElement("hitlist",m_template);

      Statement statement =
                         conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                              ResultSet.CONCUR_UPDATABLE);

      statement.setMaxRows(m_maxRows);

      ResultSet rs = statement.executeQuery(query);
      ResultSetMetaData rsmd = rs.getMetaData();
      int count = rsmd.getColumnCount();

      //Calculate the cursor position based on the page number
      if (pageNo > 0)
      {
        rowCursor = (pageLen * pageNo) - pageLen;
      }
      else
      {
        rowCursor = 1;

        if(rs.last())
        {
          pageLen = rs.getRow();
        }

        rs.first();
      }

      // Set the Cursor position only if page number is greater than zero
      if(pageNo != 1 && pageNo > 0)
      {
        rs.absolute(rowCursor);
      }

      while(rs.next() && imax < pageLen)
      {
        Node row = getRowClone();
        String rsVal = null;

        for(int k = 1; k <= count; k++)
        {
          if(hiddenFields.contains(new Integer(k)))
          {
            continue;
          }
          else
          {
            Node col = getColClone();

            StringBuffer buffer = null;

            Field field = m_fieldManager.getFieldAt(k-1);
            Vector urlFields = getUrlFields(functionId,field,urlConn);

            if(urlFields.size() > 0)
            {
              buffer = new StringBuffer();

              if(urlFields.size() == 1)
              {
                Field urlField = (Field)urlFields.elementAt(0);
                String urlVal = rs.getString(urlField.getColumn());

                if(urlVal.startsWith("http"))
                {
                  buffer.append(urlVal);
                }
                else
                {
                  buffer.append("javascript:");
                  buffer.append("Add(");
                  buffer.append("'"+urlField.getFieldCd()+"',");
                  buffer.append(urlVal+");");
                  buffer.append("BuildNewUrlArray();");
                }
              }
              else
              {
                buffer.append("javascript:");

                for(int j = 0; j < urlFields.size(); j++)
                {
                  Field urlField = (Field)urlFields.elementAt(j);
                  buffer.append("Add('"+
                                urlField.getFieldCd()+"',");
                  buffer.append(rs.getString(urlField.getColumn())+");");
                }

                buffer.append("BuildNewUrlArray();");
              }
            }

            if(buffer !=  null)
            {
              Node url = m_dom.getElement("url",(Element)col);
              url.appendChild(m_template.createTextNode(buffer.toString()));
            }

            rsVal = rs.getString(rsmd.getColumnName(k));

            if(rsVal != null)
            {
              Node fval = m_dom.getElement("fval",(Element)col);
              fval.appendChild(m_template.createTextNode(rsVal));
            }

            row.appendChild(col);
          }
        }

        Node origCol = m_dom.getElement("col",(Element)row);
        row.removeChild(origCol);

        hitlist.appendChild(row);
        imax++;
      }

      if(rs.last())
      {
        totalCount = rs.getRow();
      }

      rs.close();
      statement.close();

      Node origRow = m_dom.getElement("row",m_template);
      hitlist.removeChild(origRow);

      m_jdbcResourcePool.releaseResource(urlConn);
    }
    catch (ResourcePoolException rpe)
    {
      throw new SQLException("Unable to access JDBCResourcePool: "+
                             rpe.getMessage());
    }
  }


  /**
   * Executes the select statement and populates the template.
   *
   * @param  doc  Document containing parameters for select statement.
   * @param  functions  The NextGenFunctions container this function lives in.
   */

  public Document executeForDocument(Document doc,
                                     NextGenFunctions functions)
                                                       throws APIException
  {
    int pNum =0;
    Connection conn = null;

    try
    {
      conn = (Connection)m_jdbcResourcePool.getResource();
      m_template = NextGenEnv.getTemplate(m_templateName);

      Integer functionId = new Integer(
                           m_dom.getTextNodeValue("function_id",doc));
      String reqType = m_dom.getTextNodeValue("req_type",doc);
      Integer profileId = new Integer(
                           m_dom.getTextNodeValue("profile_id",doc));
      Integer participantId = new Integer(
                           m_dom.getTextNodeValue("participant_id",doc));

      initFields(functionId,conn);

      String query = buildQuery(doc);
      m_logger.log(Level.FINE,"Function_id "+functionId+" using SQL query: "+
                   query);

      Node pNo = m_dom.getElement("page_no",doc);

      if (pNo !=null)
      {
        pNum = getPageNumber(doc);
      }
      else
      {
        pNum = 1;
      }

      Vector hiddenFields = new Vector();
      createHeaders(functionId,profileId,participantId,conn,hiddenFields);
      createRows(query,getPageLength(functionId,conn),pNum,functionId,conn,
                 hiddenFields);
      createResponseType(reqType,profileId,participantId);
      createNavigation(getPageLength(functionId,conn),pNum);
      setTitle(functionId,conn);

      //boolean bPage = isLastPage(pageLen, pageNo);

      hiddenFields = null;
      return m_template;
    }
    catch (SQLException sqle)
    {
      m_logger.log(Level.SEVERE,"Encountered SQLException: "+sqle.getMessage(),
                   sqle);
      throw new APIException(sqle);
    }
    catch (ResourcePoolException rpe)
    {
      m_logger.log(Level.SEVERE,"Encountered ResourcePool: "+rpe.getMessage(),
                   rpe);
      throw new APIException(rpe);
    }
    finally
    {
      try
      {
        m_fieldManager.destroy();
        m_fieldManager = null;

        if(conn != null)
        {
          m_jdbcResourcePool.releaseResource(conn);
          m_logger.log(Level.INFO,"Releasing connection back to pool");
        }
      }
      catch (ResourcePoolException rpe)
      {
        throw new APIException(rpe);
      }
    }
  }


  /**
   * Not implemented.
   */

  public String executeForString(Document doc,
                                 NextGenFunctions functions) throws APIException
  {
    return null;
  }


  //members
  /**
   * Convenience DOM functions.
   */

  protected DomImpl m_dom = null;


  /**
   * Provides methods to extract relevant information from the fields hash map.
   */

  protected FieldManager m_fieldManager = null;


  private static Logger m_logger =
                   Logger.getLogger("uti.nextgen.eservices.BaseSQLFunction");

  private int m_maxRows = 0;
  private int totalCount = 0;
  private Document m_template = null;
  private String m_fieldSelect = null;
  private String m_responseSelect = null;
  private String m_titleSelect = null;
  private String m_headerSelect = null;
  private String m_templateName = null;
  private LinkedList m_allowedList = null;
  private LinkedHashMap m_fieldMap = null;
  private String m_pageLengthSelect = null;
  private String m_singleFieldSelect = null;
  private String m_drilldownIdSelect = null;
  private String m_drilldownFieldSelect = null;
  private JDBCResourcePool m_jdbcResourcePool = null;
}
