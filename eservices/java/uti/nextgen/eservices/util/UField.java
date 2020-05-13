/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.eservices.util;

/**
 * Utility class that encapsulates all metadata regarding a specific query
 * field.
 * <p> 
 * This class is modelled around the data that is stored in the ufield database
 * table.
 *
 * @author  Sanjeev Sharma
 */

public class UField extends Object
{
  /**
   * Constructs a new UField with all the required information.
   *
   * @param  fieldId       Unique ID.
   * @param  name          Descriptive name.
   * @param  schame        Database schema where field is defined.
   * @param  table         Table where field is defined.
   * @param  column        The column name used to represent this field in the 
   *                       database.
   * @param  type          The value type of this field.
   * @param  length        The value size of this field.
   * @param  precision     The value precision of this field.
   * @param  functionCall  Field specific function call 
   * @param  funcionAlias  Field specific function alias 
   */

  public UField(Integer fieldId,
                String name,
                String schema,
                String table,
                String column,
                String type,
                Integer length,
                Integer precision,
                String functionCall,
                String functionAlias)
  {
    m_fieldId = fieldId;
    m_name = name;
    m_schema = schema;
    m_table = table;
    m_column = column;
    m_type = type;
    m_length = length;
    m_precision = precision;
    m_functionCall = functionCall;
    m_functionAlias = functionAlias;

    m_fullName = schema+"."+table+"."+column;
    m_fullTableName = schema+"."+table;
    
    if((functionCall != null) && (functionAlias != null))
    {
      m_functionCall = m_functionCall.replaceFirst("@1",m_fullName);  
    }
  }


  /**
   * Returns the field id of this field.
   *
   * @return Integer
   */

  public Integer getFieldId()
  {
    return m_fieldId;
  }


  /**
   * Returns the descriptive name of this field.
   *
   * @return  String
   */

  public String getName()
  {
    return m_name;
  }


  /**
   * Returns the schema name where this field is defined.
   *
   * @return  String
   */

  public String getSchema()
  {
    return m_schema;
  } 


  /**
   * Returns the table name where this field is defined.
   *
   * @return  String
   */

  public String getTable()
  {
    return m_table;
  }


  /**
   * Returns the column name used to represent this field in the database.
   *
   * @return  String
   */

  public String getColumn()
  {
    return m_column;
  }


  /**
   * Returns the value type of this field.
   *
   * @return  String
   */

  public String getType()
  {
    return m_type;
  }


  /**
   * Returns the value length or size of this field.
   *
   * @return  Integer
   */

  public Integer getLength()
  {
    return m_length;
  }


  /**
   * Returns the value precision of this field.
   *
   * @return  Integer 
   */

  public Integer getPrecision()
  {
    return m_precision;
  }


  /**
   * Return the function call for this field.
   *
   * @return  String containing function call.
   */

  public String getFunctionCall()
  {
    return m_functionCall;
  }


  /**
   * Return the function alias for this field.
   *
   * @return  String containing function alias.
   */

  public String getFunctionAlias()
  {
    return m_functionAlias;
  }


  /**
   * Returns the full name of this field. That is: schema+table+column.
   *
   * @return  String containing full name or fuction call.
   */

  public String getFullName()
  {
    return m_fullName;
  }


  /**
   * Returns the full name of the table. That is: schema+table.
   *
   * @return  String
   */

  public String getFullTableName()
  {
    return m_fullTableName;
  }


  /**
   * Indicates whether or not this field has a valid functionCall/alias pair.
   *
   * @return boolean indicating the existance of a valid functionCall/alias 
   * pair.
   */

  public boolean hasValidFunctionCall()
  {
    if((m_functionCall == null) || (m_functionAlias == null))
    {
      return false;
    }
    else 
    {
      return true;
    }
  }


  //members
  private String m_name = null;
  private String m_type = null;
  private String m_table = null;
  private String m_schema = null;
  private String m_column = null;
  private Integer m_length = null;
  private Integer m_fieldId = null;
  private String m_fullName = null;
  private Integer m_precision = null;
  private String m_functionCall = null;
  private String m_functionAlias = null;
  private String m_fullTableName = null;
}
