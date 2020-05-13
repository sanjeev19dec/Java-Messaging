/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.eservices.util;

import java.util.*;

/**
 * Provides functionality to exctract relevant information from the hash map
 * containing all the query fields.  The query fields managed by this class is
 * is for output purposes.  That is: It stores the set of fields configured in
 * either the default_output or profile_output tables for a specific service id.
 * <p>
 * This class will ease the development of dynamic SQL query functions.
 *
 * @author  Sanjeev Sharma
 */

public class FieldManager extends Object
{
  /**
   * Constructs a new manager with the given hash map.  The hash map contains
   * field id's as keys and Field instances as values.
   *
   * @param  fieldMap  LinkedHashMap containing query fields.
   */

  public FieldManager(LinkedHashMap fieldMap)
  {
    m_fieldMap = fieldMap;
  }


  /**
   * Returns all the full names of all the fields contained in the hash map.
   *
   * @return  LinkedList of all full names.
   */

  public LinkedList getFullNames()
  {
    LinkedList returnList = new LinkedList();
    LinkedList values = new LinkedList(m_fieldMap.values());
    ListIterator iter = values.listIterator();

    while(iter.hasNext())
    {
      Field field = (Field)iter.next();

      if(field.hasValidFunctionCall())
      {
        returnList.add(field.getFunctionCall()+" "+field.getFunctionAlias());
      }
      else
      {
        returnList.add(field.getFullName());
      }
    }

    return returnList;
  }


  /**
   * Returns all the full table names of all the fields contained in the hash
   * map.
   *
   * @return  LinkedList of all full table names.
   */

  public LinkedList getFullTableNames()
  {
    LinkedList returnList = new LinkedList();
    LinkedList values = new LinkedList(m_fieldMap.values());
    ListIterator iter = values.listIterator();

    while(iter.hasNext())
    {
      Field field = (Field)iter.next();
      String tableName = field.getFullTableName();

      if(!(returnList.contains(tableName)))
      {
        returnList.add(field.getFullTableName());
      }
    }

    return returnList;
  }


  /**
   * Returns a list of function calls and aliases for all the fields contained
   * in the hash map that has valid function call/alias pairs.
   *
   * @return LinkedList containining function calls in the form "call alias"
   */

  public LinkedList getFunctionCalls()
  {
    LinkedList returnList = new LinkedList();
    LinkedList values = new LinkedList(m_fieldMap.values());
    ListIterator iter = values.listIterator();

    while(iter.hasNext())
    {
      Field field = (Field)iter.next();

      if(field.hasValidFunctionCall())
      {
        returnList.add(field.getFunctionCall()+" "+field.getFunctionAlias());
      }
    }

    return returnList;
  }


  /**
   * Determines whether or not the given table name is contained within one of
   * the query fields.
   *
   * @param  tableName  Name of table to check for.  Can only be the name of a
   *                    table or in the form schema.table.
   *
   * @return boolean  Indicating whether or not the given table name exists
   *                  within one of the query fields.
   */

  public boolean containsTable(String tableName)
  {
    tableName = tableName.toLowerCase();

    LinkedList values = new LinkedList(m_fieldMap.values());
    ListIterator iter = values.listIterator();

    while(iter.hasNext())
    {
      Field field = (Field)iter.next();

      if((field.getTable().toLowerCase()).equals(tableName))
      {
        return true;
      }
      else if((field.getFullTableName().toLowerCase()).equals(tableName))
      {
        return true;
      }
    }

    return false;
  }


  /**
   * Returns the field at the given position in this manager.
   *
   * @param  index  Index where field lives.
   *
   * @return  Field at given index.
   */

  public Field getFieldAt(int index)
  {
    LinkedList list = new LinkedList(m_fieldMap.values());
    return (Field)list.get(index);
  }


  /**
   * Returns the field with the given field id.
   *
   * @param  id  Field id.
   *
   * @return  Field instance with the given field id.
   */

  public Field getField(String id)
  {
    return (Field)m_fieldMap.get(id);
  }


  /**
   * Destroys the manager.
   */

  public void destroy()
  {
    m_fieldMap = null;
  }


  //members
  private LinkedHashMap m_fieldMap = null;
}
