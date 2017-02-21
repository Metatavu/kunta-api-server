package fi.otavanopisto.kuntaapi.server.discover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class EntityUpdateRequestQueue<T extends AbstractEntityUpdateRequest<?>> {

  private static Logger logger = Logger.getLogger(EntityUpdateRequestQueue.class.getName());
  private List<T> queue = Collections.synchronizedList(new ArrayList<>());
  
  /**
   * Prepends request in the front of the queue. If any existin update requests 
   * exists in the queue they are removed. 
   * 
   * OrderIndex is ignored when finding an existing update request
   * 
   * @param entityUpdateRequest
   */
  public void prepend(T entityUpdateRequest) {
    if (checkUpdateRequest(entityUpdateRequest)) {
      remove(entityUpdateRequest);
      queue.add(0, entityUpdateRequest);      
    }
  }
  
  /**
   * Appends request at the end of the queue. If update request for same id already
   * exist in the queue, its replaced with new one.
   * 
   * OrderIndex is ignored when finding an existing update request
   * 
   * @param entityUpdateRequest
   */
  public void append(T entityUpdateRequest) {
    if (checkUpdateRequest(entityUpdateRequest)) {
      int index = findIndex(entityUpdateRequest);
      if (index == -1) {
        queue.add(entityUpdateRequest);
      } else {
        queue.remove(index);
        queue.add(index, entityUpdateRequest);
      }
    }
  }
  
  /**
   * Appends or prepends the request into the queue regarding whether 
   * its a priority request or not
   * 
   * @param entityUpdateRequest
   */
  public void add(T entityUpdateRequest) {
    if (entityUpdateRequest == null) {
      logger.severe("Tried to add null update request into the queue");
      return;
    }
    
    if (entityUpdateRequest.isPriority()) {
      prepend(entityUpdateRequest);
    } else {
      append(entityUpdateRequest);
    }
  }
  
  public T next() {
    if (!queue.isEmpty()) {
      return queue.remove(0);
    }
    
    return null;
  }

  /**
   * Removes update requests for the id from the queue
   * 
   * @param id
   */
  public void remove(T entity) {
    int index = findIndex(entity);
    if (index >= 0) {
      queue.remove(index);
    }
  }

  private boolean checkUpdateRequest(T entityUpdateRequest) {

    if (entityUpdateRequest == null) {
      logger.severe("Tried to add null update request into queue");
      return false;
    }
    
    return true;
  }
  
  private int findIndex(T entity) {
    for (int i = 0, l = queue.size(); i < l; i++) {
      if (queue.get(i).equals(entity)) {
        return i;
      }
    }
    
    return -1;
  }

}
