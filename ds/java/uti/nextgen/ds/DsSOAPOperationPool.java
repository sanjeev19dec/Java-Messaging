/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.ds;

import java.util.*;
import uti.nextgen.tools.*;

/**
 * ResourcePool implementation for DsSOAPOperation instances.  This will enable
 * DsSOAPOperation to be used in a thread safe manner.
 *
 * @author  Sanjeev Sharma
 */

public class DsSOAPOperationPool extends ResourcePoolImpl
{
  /**
   * Default constructor
   */

  public DsSOAPOperationPool()
  {
  }


  /**
   * Creates a DsSOAPOperation as a resource for this pool implementation.
   *
   * @param  xmlProps  XMLConfiguration properties.
   *
   * @throws  ResourcePoolException if the DsSOAPOperation instance cannot be
   *          created. 
   */

  protected Object createResource(XMLProperties xmlProps)
                                                  throws ResourcePoolException
  {
    try
    {
      DsSOAPOperation operation = new DsSOAPOperation(xmlProps);
      return operation;
    }
    catch (Exception e)
    {
      throw new ResourcePoolException(e);
    }
  }
 

  /**
   * Destroys the DsSOAPOperation instances.
   *
   * @param  resource  DsSOAPOperation to destroy.
   *
   * @throws ResourcePoolException
   */

  protected void destroyResource(Object resource) throws ResourcePoolException
  {
    resource = null; 
  }
}
