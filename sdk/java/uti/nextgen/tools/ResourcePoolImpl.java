/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.tools;

import java.util.*;
import org.w3c.dom.*;

/**
 * A generic implementation of ResourcePool interface.  Provides abstract 
 * methods that must be overridden to do resource specific creation,validation 
 * and cleanup.
 *
 * @author  Sanjeev Sharma
 */

public abstract class ResourcePoolImpl implements ResourcePool
{
  /**
   * Subclasses of this class must implement this method with resource specific
   * initialisation code.  This method will be called from init and get methods
   * to create resources.
   *
   * @param  xmlProps  XML configuration properties.
   *
   * @throws  ResourcePoolException if an exception occurs while creating a 
   *          resource.
   */

  protected abstract Object createResource(XMLProperties xmlProps)
                                                  throws ResourcePoolException;
  

  /**
   * Subclasses of this class must implement this method with resource specific
   * cleanup code.  This method will called from the destroy method.
   *
   * @param  resource  Resource to destroy.
   *
   * @throws  ResourcePoolException if the given resource cannot be destroyed.
   */

  protected abstract void destroyResource(Object resource) 
                                                  throws ResourcePoolException;
  

  /**
   * Initialises the member and lists, sizes and initial resources for this
   * pool.
   *
   * @param  name      Name of this pool.
   * @param  xmlProps  XML configuration properties. 
   *
   * @throws  ResourcePoolException if the pool cannot be initialized.
   */

  public void init(String name,
                   XMLProperties xmlProps) throws ResourcePoolException
  {
    try
    {
      m_xmlProps = xmlProps;
      Element conf = xmlProps.getElementByNameAttr("ResourcePool",name); 

      m_availableList = new LinkedList();
      m_busyList = new LinkedList();

      m_maxSize = Integer.parseInt(xmlProps.getTextNodeValue(conf,"MaxSize"));
      m_initSize = 
           Integer.parseInt(xmlProps.getTextNodeValue(conf,"InitialSize"));

      for(int i = 0; i < m_initSize; i++)
      {
        m_availableList.add(createResource(m_xmlProps));
      }
    }
    catch (ConfException ce)
    {
      throw new ResourcePoolException(ce);
    }
  }


  /**
   * Returns a resource from this pool. A resource is returned in the following
   * manner:
   * <p>
   * The available list is checked for resources. If none are available a new 
   * resource is created if the maximum number of resources have not yet been 
   * reached. 
   * <p>
   * If the maximum number of resources have been reached this call blocks 
   * until a resource becomes available.
   *
   * @return  Object containing resource.
   *
   * @throws  ResourcePoolException if the a resource could not be returned.
   */

  public synchronized Object getResource() throws ResourcePoolException
  {
    int resourceCount = m_availableList.size() + m_busyList.size();

    if(m_availableList.size() > 0)
    {
      Object resource = m_availableList.removeFirst();
      m_busyList.add(resource);

      return resource;
    }
    else if((m_availableList.size() == 0) && (resourceCount < m_maxSize))
    {
      m_availableList.add(createResource(m_xmlProps));
      return getResource();
    }
    else
    {
      try
      {
        wait();
        return getResource();
      }
      catch (InterruptedException ie)
      {
        throw new ResourcePoolException(ie);
      }
    }
  }
 

  /**
   * Releases a resource from this pool.
   *
   * @param  resource  Resource to release back to the pool. 
   *
   * @throws  ResourcePoolException if the resource cannot be released back to
   *          the pool.
   */

  public synchronized void releaseResource(Object resource) 
                                                  throws ResourcePoolException
  {
    boolean removed = m_busyList.remove(resource);
   
    if(removed)
    {
      m_availableList.add(resource);
      notifyAll();
    }
    else
    {
      throw new ResourcePoolException(
              "Could not release resource back to pool: "+resource.toString());
    }
  }


  /**
   * Destroy this pool
   *
   * @throws ResourcePoolException if the pool cannot be destroyed. 
   */

  public void destroy() throws ResourcePoolException
  {
    for(int i = 0; i < m_availableList.size(); i++)
    {
      destroyResource(m_availableList.remove(i));
    }

    for(int j = 0; j < m_busyList.size(); j++)
    {
      destroyResource(m_busyList.remove(j));
    }
  }


  //members
  private int m_maxSize = 0;
  private int m_initSize = 0;
  private LinkedList m_busyList = null;
  private XMLProperties m_xmlProps = null;
  private LinkedList m_availableList = null;
}
