/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.adapter;

import java.util.*;

/**
 * FILO Queue implementation 
 *
 * @author  Sanjeev Sharma
 */

public class Queue extends Object
{
  /**
   * Constructs a new Queue by initializing the member LinkedList
   */

  public Queue()
  {
    m_list = new LinkedList();
  }


  /**
   * Constructs a new Queue with the given maximum depth
   *
   * @param  maxDepth  Maximum depth this queue is allowed to reach.
   */

  public Queue(int maxDepth)
  {
    this();
    m_maxDepth = maxDepth;
  }


  /**
   * Places a new element in the queue or discards the event if the maximum
   * depth has been reached.
   *
   * @param  obj  Object to add to queue
   */

  public synchronized void push(Object obj) throws QueueDiscardWarning
  {
    if(m_list.size() > m_maxDepth)
    {
      throw new QueueDiscardWarning("Queue reached maximum depth of "+
                                    m_maxDepth+", discarding event");
    }
    else
    {
      m_list.addLast(obj);
      notifyAll();
    }
  }


  /**
   * Removes an element from the queue and returns it
   *
   * @return  Element at the top of the queue.
   * @throws   java.lang.InterruptedException
   */

  public synchronized Object pop() throws InterruptedException
  {
    while(m_list.isEmpty())
    {
      wait();
    }
    
    return m_list.removeFirst();
  }

 
  /**
   * Returns the number of events contained in the queue.
   *
   * @return  Number of element queue.
   */

  public int getSize()
  {
    return m_list.size();
  }


  //members
  private int m_maxDepth = 50;
  private LinkedList m_list = null;
}
