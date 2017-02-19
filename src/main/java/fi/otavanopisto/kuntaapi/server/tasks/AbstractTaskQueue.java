package fi.otavanopisto.kuntaapi.server.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
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
  
  /**
   * Returns unique name for task queue
   * 
   * @return unique name for task queue
   */
  public abstract String getName();

  /**
   * Returns next task or null if queue is empty
   * 
   * @return next task or null if queue is empty
   */
  public T next() {
    startBatch();
    try {
      List<String> priorities = getPriorities();
      if (priorities.isEmpty()) {
        return null;
      }
      
      String id = priorities.remove(0);
      setPriorities(priorities);
      
      return popTask(id);
    } finally {
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
      Cache<String,byte[]> tasksCache = getTasksCache();
      List<String> priorities = getPriorities();
      
      String id = createUniqueTaskId();
      if (priority) {
        priorities.add(0, id);
      } else {
        priorities.add(id);
      }
      
      setPriorities(priorities);
      tasksCache.put(id, rawData);
    } finally {
      endBatch();
    }
  }

  private void startBatch() {
    getPrioritiesCache().getAdvancedCache().startBatch();
    getTasksCache().getAdvancedCache().startBatch();
  }

  private void endBatch() {
    getPrioritiesCache().getAdvancedCache().endBatch(true);
    getTasksCache().getAdvancedCache().endBatch(true);
  }

  private String createTaskId() {
    Random random = new Random();
    byte[] bytes = new byte[6];
    random.nextBytes(bytes);
    return Base64.encodeBase64String(bytes);
  }
  
  private String createUniqueTaskId() {
    Cache<String,byte[]> tasksCache = getTasksCache();
    while (true) {
      String id = createTaskId();
      if (!tasksCache.containsKey(id)) {
        return id;
      }
    }
  }
  
  private T popTask(String id) {
    Cache<String, byte[]> tasksCache = getTasksCache();
    byte[] rawData = tasksCache.remove(id);
    if (rawData == null) {
      return null;
    }
    
    return unserialize(rawData);
  }
  
  private List<String> getPriorities() {
    Cache<String,List<String>> prioritiesCache = getPrioritiesCache();
    List<String> result = prioritiesCache.get(getName());
    
    if (result == null) {
      return new ArrayList<>();
    }
    
    return result;
  }
  
  private void setPriorities(List<String> priorities) {
    getPrioritiesCache().put(getName(), priorities);
  }
  
  private Cache<String, byte[]> getTasksCache() {
    return cacheContainer.getCache("tasks");
  }
  
  private Cache<String, List<String>> getPrioritiesCache() {
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
