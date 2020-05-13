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

/**
 * Interface that describes a generic resource pool. 
 *  
 * @author  Hannes Holthausen
 */

public interface ResourcePool
{
  /**
   * Initialise the resources that the pool implementation will provide here.
   *
   * @param  name      Unique name for this pool.
   * @param  xmlProps  XML configuration properties.
   *
   * @throws  ResourcePoolException if the pool cannot be initialised. 
   */

  public void init(String name,
                   XMLProperties xmlProps) throws ResourcePoolException;


  /**
   * Returns a resource from this pool 
   * 
   * @return  Object reference to resource.
   *
   * @throws  ResourcePoolException if a resource could not be returned from 
   *          the pool.
   */

  public Object getResource() throws ResourcePoolException; 


  /**
   * Release a resource back into the pool.
   *
   * @param  resource  Object being released to the pool.
   *
   * @throws  ResourcePoolException if the resource cannot be released back to
   *          the pool.
   */

  public void releaseResource(Object resource) throws ResourcePoolException;


  /**
   * Destroy this resource pool.
   *
   * @throws  ResourcePoolException if the pool cannot be destroyed.
   */

  public void destroy() throws ResourcePoolException;
}
