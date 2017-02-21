package fi.otavanopisto.kuntaapi.server.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

/**
 * Abstract base class for all task queues
 * 
 * @author Antti Leppä
 *
 * @param <T> task type
 */
public abstract class AbstractTaskQueue <T extends AbstractTask> {
  
  @Inject
  private Logger logger;

  @Resource (lookup = "java:jboss/infinispan/container/kunta-api")
  private CacheContainer cacheContainer;
  
  private long tasksExecuted;
  private long duplicatedTasks;
  
  /**
   * Returns unique name for task queue
   * 
   * @return unique name for task queue
   */
  public abstract String getName();
  
  /**
   * Returns statistics for the queue
   * 
   * @return statistics for the queue
   */
  public TaskQueueStatistics getStatistics() {
    return new TaskQueueStatistics(getName(), tasksExecuted, duplicatedTasks, getPriorities().size());
  }

  /**
   * Returns next task or null if queue is empty
   * 
   * @return next task or null if queue is empty
   */
  public T next() {
    startBatch();
    try {
      List<Integer> priorities = getPriorities();
      if (priorities.isEmpty()) {
        return null;
      }
      
      Integer taskHashId = priorities.remove(0);
      setPriorities(priorities);
      
      return popTask(taskHashId);
    } finally {
      tasksExecuted++;
      endBatch();
    }
  }
  
  /**
   * Enqueus new task to the queue. If priority flag is true, the task will be prepended to 
   * the front of the queue otherwise the task will be appened to the end of the queue
   * 
   * @param priority whether the task is a priority task or not
   * @param task taks
   */
  public void enqueueTask(boolean priority, T task) {
    startBatch();
    try {
      byte[] rawData = serialize(task);
      Cache<String, byte[]> tasksCache = getTasksCache();
      List<Integer> priorities = getPriorities();
      
      Integer taskHashId = task.getTaskHash();
      if (priority) {
        if (priorities.contains(taskHashId)) {
          duplicatedTasks++;
          priorities.remove(taskHashId);
        }
        
        priorities.add(0, taskHashId);
      } else {
        if (!priorities.contains(taskHashId)) {
          priorities.add(taskHashId);
        }
      }
      
      setPriorities(priorities);
      tasksCache.put(createTaskId(taskHashId), rawData);
    } finally {
      endBatch();
    }
  }

  private String createTaskId(Integer taskHashId) {
    return String.format("%s-%d", getName(), taskHashId);
  }

  private void startBatch() {
    getPrioritiesCache().getAdvancedCache().startBatch();
    getTasksCache().getAdvancedCache().startBatch();
  }

  private void endBatch() {
    getPrioritiesCache().getAdvancedCache().endBatch(true);
    getTasksCache().getAdvancedCache().endBatch(true);
  }

  private T popTask(Integer id) {
    Cache<String, byte[]> tasksCache = getTasksCache();
    byte[] rawData = tasksCache.remove(createTaskId(id));
    if (rawData == null) {
      return null;
    }
    
    return unserialize(rawData);
  }
  
  private List<Integer> getPriorities() {
    Cache<String,List<Integer>> prioritiesCache = getPrioritiesCache();
    List<Integer> result = prioritiesCache.get(getName());
    
    if (result == null) {
      return new ArrayList<>();
    }
    
    return result;
  }
  
  private void setPriorities(List<Integer> priorities) {
    getPrioritiesCache().put(getName(), priorities);
  }
  
  private Cache<String, byte[]> getTasksCache() {
    return cacheContainer.getCache("tasks");
  }
  
  private Cache<String, List<Integer>> getPrioritiesCache() {
    return cacheContainer.getCache("task-priorities");
  }
  
  @SuppressWarnings ("squid:S1168")
  private byte[] serialize(T task) {
    try (ByteArrayOutputStream resultStream = new ByteArrayOutputStream()) {
      serializeToStream(task, resultStream);
      resultStream.flush();
      return resultStream.toByteArray();
    } catch (IOException e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to write serialized task data", e);
      }
    }
    
    return null;
  }

  private void serializeToStream(T task, ByteArrayOutputStream resultStream) {
    try (ObjectOutputStream objectStream = new ObjectOutputStream(resultStream)) {
      objectStream.writeObject(task);
      objectStream.flush();
    } catch (IOException e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to serialize task", e);
      }
    }
  }

  private T unserialize(byte[] rawData) {
    try (ByteArrayInputStream byteStream = new ByteArrayInputStream(rawData)) {
      return unserializeFromStream(byteStream);
    } catch (IOException e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to write unserialized task data", e);
      }
    }
    
    return null;
  }

  @SuppressWarnings("unchecked")
  private T unserializeFromStream(ByteArrayInputStream byteStream) {
    try (ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
      Object object = objectStream.readObject();
      if (object == null) {
        return null;
      }
      
      return (T) object;
    } catch (IOException | ClassNotFoundException e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to unserialize task", e);
      }
    }
    
    return null;
  }
  
}
