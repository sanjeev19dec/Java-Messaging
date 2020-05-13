/*
 * Id: $Id: DirObjectStateFactory.java,v 1.1 2003/05/29 15:03:05 hannesh Exp $
 * 
 * $RCSfile: DirObjectStateFactory.java,v $ $Revision: 1.1 $ $Date: 2003/05/29 15:03:05 $ 
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
 * This class is an implementation of the JNDI DirStateFactory interface.  It 
 * provides a way of committing DirObject instances to a directory. 
 *
 * @author  Sanjeev Sharma
 */

public class DirObjectStateFactory implements DirStateFactory
{
  /**
   * Default constructor
   */
  
  public DirObjectStateFactory()
  {
  }


  /**
   * Transforms the given DirObject into a set of naming attributes. This 
   * process is called "retrieving the objects state to bind."
   * <p>
   * The naming attributes are created based on the DirObject configuration and
   * the values of the DirAttributes contained in the DirObject.
   *
   * @param  obj      DirObject instance whose state is to be retrieved.
   * @param  name     Name of this object, or null if no name was given.
   * @param  nameCtx  The context relative to which the name parameter was 
   *                  specified or null if the name is relative to the initial
   *                  context.
   * @param  env      Environment to be used when creating the object's state.
   *                  Can be null.
   * @param  inAttrs  Attributes to be bound with the object. Can be null.
   *
   * 
   * @return  DirStateFactory.Result containing the naming attributes and state    *          for the given DirObject.  Null is returned if the given object is
   *           not a DirObject instance.
   *
   * @throws  NamingException if the DirObject cannot be converted into a set
   *          of naming attributes.
   */

  public DirStateFactory.Result getStateToBind(Object obj,
                                              Name name,
                                              Context nameCtx,
                                              Hashtable env,
                                              Attributes inAttrs) 
                                                        throws NamingException
  {
    if(obj instanceof DirObject)
    {
      Attributes outAttrs;
      DirObject dirObject = (DirObject)obj;

      if(inAttrs == null)
      {
        outAttrs = new BasicAttributes(true);
      }
      else
      {
        outAttrs = (Attributes)inAttrs.clone();
      }

      BasicAttribute objectClass = new BasicAttribute("objectclass");
      Vector ocValues = dirObject.getAttributeValues("objectclass");

      for(int i = 0; i < ocValues.size(); i++)
      {
        objectClass.add((String)ocValues.elementAt(i));
      }

      outAttrs.put(objectClass);
      Enumeration dirAttrs = dirObject.getAttributes();

      while(dirAttrs.hasMoreElements())
      {
        DirAttribute dirAttr = (DirAttribute)dirAttrs.nextElement();
  
        if(!((dirAttr.getName()).equals("objectclass")))
        {
          if((dirAttr.isRequired()) && (dirAttr.isEmpty()))
          {
            throw new SchemaViolationException("Must have attr: "+
                                               dirAttr.getName());
          }
          else if(!(dirAttr.isEmpty()))
          {
            BasicAttribute attr = new BasicAttribute(dirAttr.getName());
            Vector values = dirAttr.getValues();

            for(int i = 0; i < values.size(); i++)
            {
              attr.add(values.elementAt(i));
            }

            outAttrs.put(attr);
          }
        }
      }

      return new DirStateFactory.Result(null,outAttrs);
    }
    else
    {
      return null;
    }
  }


  /**
   * Not implemented.
   */

  public Object getStateToBind(Object obj,
                               Name name,
                               Context ctx,
                               Hashtable env) throws NamingException
  {
    return null;
  }
}
