/*
 * Id: $Id: DirObjectObjectFactory.java,v 1.2 2003/06/05 16:22:39 hannesh Exp $
 * 
 * $RCSfile: DirObjectObjectFactory.java,v $ $Revision: 1.2 $ $Date: 2003/06/05 16:22:39 $ 
 *
 * Description: 
 *
 */

package uti.nextgen.lprovision;

import java.util.*;
import javax.naming.*;
import javax.naming.spi.*;
import javax.naming.directory.*;

/**
 * This class is an implementation of the JNDI DirObjectFactory interface. 
 * It provides a way of reading DirObject intances from the directory.
 *
 * @author  Sanjeev Sharma
 */

public class DirObjectObjectFactory implements DirObjectFactory
{
  /**
   * Default constructor
   */

  public DirObjectObjectFactory()
  {
  }


  /**
   * Creates a DirObject instance from the given attributes and returns it.
   *
   * @param  obj      Object containing reference of location information. 
   *                  Can be null.
   * @param  name     Name of this object relative to nameCtx or null if no name
   *                  was specified.
   * @param  nameCtx  The context relative to the name parameter or null if 
   *                  name is relative to the initial context.
   * @param  env      Environment settings.  Can be null.
   * @param  attrs    Attributes to bind to the object being created.
   *
   * return  DirObject instance.
   *
   * @throws  Exception if the DirObject instance could not be created.
   */

  public Object getObjectInstance(Object obj,
                                  Name name,
                                  Context nameCtx,
                                  Hashtable env,
                                  Attributes attrs) throws Exception
  {
    DirObject tmpObj = new DirObject(); 
    DirAttribute tmpOcAttr = new DirAttribute(
                                         "objectclass","yes","no","no",null);

    Attribute objectClass = attrs.get("objectclass");
    NamingEnumeration ocValues = objectClass.getAll();

    while(ocValues.hasMore())
    {
      tmpOcAttr.addValue((String)ocValues.next());
    }

    ocValues.close();
    tmpObj.addAttribute(tmpOcAttr);

    boolean supported = false;
    DirObject dirObject = null;
    Enumeration objNames = LProvisionFactory.getDirObjectNames();
 
    while(objNames.hasMoreElements())
    {
      dirObject = LProvisionFactory.getDirObject(
                                       (String)objNames.nextElement());

      if(dirObject.equals(tmpObj))
      {
        supported = true;
        break; 
      }
    }
    
    if(supported)
    {
      Enumeration attrNames = dirObject.getAttributeNames();

      while(attrNames.hasMoreElements())
      {
        String attrName = (String)attrNames.nextElement(); 

        if(!(attrName.equals("objectclass")))
        {
          Attribute attr = attrs.get(attrName);

          if(attr != null)
          {
            DirAttribute dirAttr = dirObject.getAttribute(attrName);
            NamingEnumeration attrValues = attr.getAll();

            while(attrValues.hasMore())
            {
              dirAttr.addValue(attrValues.next());
            }
            
            attrValues.close();
          }
        }
      }

      return dirObject;
    }
    else
    {
      return null;
    }
  }


  /**
   * Not implemented.
   */

  public Object getObjectInstance(Object obj,
                                  Name name,
                                  Context ctx,
                                  Hashtable env)
  {
    return null;
  } 
}
