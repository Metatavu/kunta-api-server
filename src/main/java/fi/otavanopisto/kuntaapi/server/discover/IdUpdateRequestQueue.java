package fi.otavanopisto.kuntaapi.server.discover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.BaseId;

public class IdUpdateRequestQueue<T extends AbstractIdUpdateRequest<? extends BaseId>> {

  private static Logger logger = Logger.getLogger(IdUpdateRequestQueue.class.getName());
  private List<T> queue = Collections.synchronizedList(new ArrayList<>());
  private String source;
  
  /**
   * Creates new id update request queue
   * 
   * @param source identifier which queue is expected to contain
   */
  public IdUpdateRequestQueue(String source) {
    this.source = source;
  }
  
  /**
   * Prepends request in the front of the queue. If any existin update requests 
   * exists in the queue they are removed. 
   * 
   * OrderIndex is ignored when finding an existing update request
   * 
   * @param idUpdateRequest
   */
  public void prepend(T idUpdateRequest) {
    if (checkUpdateRequest(idUpdateRequest)) {
      remove(idUpdateRequest.getId());
      queue.add(0, idUpdateRequest);      
    }
  }
  
  /**
   * Appends request at the end of the queue. If update request for same id already
   * exist in the queue, its replaced with new one.
   * 
   * OrderIndex is ignored when finding an existing update request
   * 
   * @param idUpdateRequest
   */
  public void append(T idUpdateRequest) {
    if (checkUpdateRequest(idUpdateRequest)) {
      int index = findIndex(idUpdateRequest.getId());
      if (index == -1) {
        queue.add(idUpdateRequest);
      } else {
        queue.remove(index);
        queue.add(index, idUpdateRequest);
      }
    }
  }
  
  /**
   * Appends or prepends the request into the queue regarding whether 
   * its a priority request or not
   * 
   * @param idUpdateRequest
   */
  public void add(T idUpdateRequest) {
    if (idUpdateRequest == null) {
      logger.severe(String.format("Tried to add null update request into the queue with source %s", source));
      return;
    }
    
    if (idUpdateRequest.isPriority()) {
      prepend(idUpdateRequest);
    } else {
      append(idUpdateRequest);
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
  public void remove(BaseId id) {
    int index = findIndex(id);
    if (index >= 0) {
      queue.remove(index);
    }
  }

  private boolean checkUpdateRequest(T idUpdateRequest) {
    if (idUpdateRequest == null) {
      logger.severe(String.format(String.format("Tried to add null id update request into queue with source %s", source)));
      return false;
    }
    
    if (idUpdateRequest.getId() == null) {
      logger.severe(String.format(String.format("Tried to add update request with null id into queue with source %s", source)));
      return false;
    }
    
    if (!StringUtils.equals(idUpdateRequest.getId().getSource(), source)) {
      logger.warning(String.format(String.format("Adding an update request with source %s into queue that expects identifiers with source %s", idUpdateRequest.getId().getSource(), source)));
    }
    
    return true;
  }
  
  private int findIndex(BaseId id) {
    if (!StringUtils.equals(id.getSource(), source)) {
      logger.warning(String.format(String.format("Finding an update request with source %s from queue that contains identifiers with source %s", id.getSource(), source)));
    }
    
    for (int i = 0, l = queue.size(); i < l; i++) {
      if (queue.get(i).getId().equals(id)) {
        return i;
      }
    }
    
    return -1;
  }

}
