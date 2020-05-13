/*
 * Id: $Id: Query.java,v 1.3 2003/06/10 09:54:09 hannesh Exp $
 * 
 * $RCSfile: Query.java,v $ $Revision: 1.3 $ $Date: 2003/06/10 09:54:09 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.tools;

import java.util.*;

/**
 * Class that generates SQL query strings in Java prepared statement format.
 * <p>
 * The Query object is initialized with the name of the database table to be
 * used in the generation of the query. Fields to be queried on are added to 
 * the object as name/value pairs.
 * <p>
 * The accessor method <code>getSQL()</code> returns the query string formatted
 * in Java prepared statement format.
 * <p>
 * The accessor method <code>getValues()</code> returns an object array of the
 * field values of the query object.  
 *
 * @author Sanjeev Sharma
 */

public class Query extends Object
{
  /**
   * The Field class represents a field within the query being generated. 
   * A field is a name value pair.
   */

  class Field extends Object
  {
    /**
     * Creates a new Field object with the given name and value.
     *
     * @param  name    Field name.
     * @param  type    Indicates the type of field: single or range.
     * @param  values  Field value.
     */

    Field(String name, String type, Object values[])
    {
      m_name = name;
      m_type = type;
      m_values = values;
    }


    /**
     * Returns the name of the field.
     *
     * @return  String containing the name of the field.
     */

    public String getFieldName()
    {
      return m_name;
    }

   
    /**
     * Returns the type of this field.
     *
     * @return  String containig the type of the field.
     */

    public String getFieldType()
    {
      return m_type;
    }


    /**
     * Returns the values of the field.
     *
     * @return  Object array containing the values of the field.
     */

    public Object[] getFieldValues()
    {
      return m_values;
    }

    
    //members
    private String m_name = null;
    private String m_type = null;
    private Object m_values[] = null;
  }


  /**
   * Default constructor
   */

  public Query()
  {
  }


  /**
   * Creates a new Query object with the given database table name and 
   * additional information that must be added to the generated query e.g
   * order by clauses.
   *
   * @param  prefix     Use this prefix instead of select * 
   * @param  tableName  Database table name.
   * @param  postfix    Additional information to add on to the generated query.
   */

  public Query(String prefix, String tableName, String postfix)
  {
    m_fields = new Vector();
    m_prefix = prefix;
    m_tableName = tableName;
    m_postfix = postfix;
  }


  /**
   * Adds a field to the query object.  A null value will be discarded and will
   * not form part of the generated query.
   *
   * @param  name    Name of the field to add.
   * @param  type    Type of the field to add: single or range.
   * @param  values  Values of the field to add.
   */

  public void addField(String name, String type, Object values[])
  {
    if(values != null)
    {
      m_fields.add(new Field(name,type,values));
    }
  } 


  /**
   * Generates a SQL query in Java prepared statement format.
   *
   * @return  String containing a query string in Java prepared statement 
   *          format.
   */

  public String getSQL()
  {
    String query = null;

    if(m_prefix == null)
    {
      query = "select * from "+m_tableName+" ";
    }
    else
    {
      query = m_prefix+" "+m_tableName+" ";
    } 

    for(int i = 0; i < m_fields.size(); i++)
    {
      Field field = (Field)m_fields.get(i);

      if(i == 0)
      {
        if((field.getFieldType()).equals("single"))
        {
          query = query+"where "+field.getFieldName()+" = ? ";
        }
        else if((field.getFieldType()).equals("singlenot"))
        {
          query = query+"where "+field.getFieldName()+" != ? ";
        } 
        else
        {
          query = query+"where "+field.getFieldName()+" >= ? "+
                        "and "+field.getFieldName()+" <= ? ";
        }
      }
      else
      {
        if((field.getFieldType()).equals("single"))
        {
          query = query+"and "+field.getFieldName()+" = ? ";
        }
        else if((field.getFieldType()).equals("singlenot"))
        {
          query = query+"and "+field.getFieldName()+" != ? ";
        } 
        else
        {
          query = query+"and "+field.getFieldName()+" >= ? "+
                        "and "+field.getFieldName()+" <= ? ";
        }
      }
    }

    return (query+m_postfix);
  }


  /**
   * Returns an Object array of field values contained in this query object.
   *
   * @return  Object array of field values.
   */

  public Object[] getValues()
  {
    Vector values = new Vector();

    for(int i = 0; i < m_fields.size(); i++)
    {
      Field field = (Field)m_fields.get(i);

      if(((field.getFieldType()).equals("single")) ||
         ((field.getFieldType()).equals("singlenot")))
      {
        values.add(field.getFieldValues()[0]);
      }
      else
      {
        values.add(field.getFieldValues()[0]);
        values.add(field.getFieldValues()[1]);
      }
    }

    return (values.toArray());
  }
    

  //members
  private Vector m_fields = null;
  private String m_prefix = null;
  private String m_postfix = null;
  private String m_tableName = null;
}
