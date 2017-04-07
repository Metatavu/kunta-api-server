package fi.otavanopisto.kuntaapi.server.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.persistence.dao.TaskDAO;
import fi.otavanopisto.kuntaapi.server.persistence.dao.TaskQueueDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.Task;
import fi.otavanopisto.kuntaapi.server.persistence.model.TaskQueue;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTask;

@ApplicationScoped
public class TaskController {
  
  @Inject
  private Logger logger;

  @Inject
  private TaskQueueDAO taskQueueDAO;

  @Inject
  private TaskDAO taskDAO;

  /**
   * Creates new task
   * 
   * @param <T> task type
   * @param queueName queue the task belongs to
   * @param priority whether the task is a priority task or not
   * @param task task data
   * @return created task entity
   */
  public <T extends AbstractTask> Task createTask(String queueName, Boolean priority, T task) {
    TaskQueue taskQueue = taskQueueDAO.findByName(queueName);
    if (taskQueue == null) {
      taskQueue = taskQueueDAO.create(queueName, "UNKNOWN");
    }

    byte[] data = serialize(task);
    if (data != null) {
      return taskDAO.create(taskQueue, priority, data, OffsetDateTime.now());
    }
    
    return null;
  }
  
  /**
   * Returns next tasks in queue
   * 
   * @param <T> Task type
   * @param queueName queue name
   * @param responsibleNode node that is requesting the task
   * @return next tasks in queue
   */
  public <T extends AbstractTask> T getNextTask(String queueName, String responsibleNode) {
    TaskQueue taskQueue = taskQueueDAO.findByNameAndResponsibleNode(queueName, responsibleNode);
    if (taskQueue == null) {
      return null;
    }
    
    Task task = taskDAO.findNextInQueue(taskQueue);
    
    if (task != null) {
      byte[] data = task.getData();
      taskDAO.delete(task);
      taskQueueDAO.updateLastTaskReturned(taskQueue, OffsetDateTime.now());
      return unserialize(data);
    }
    
    return null;
  }
  
  /**
   * Lists all task queues
   * 
   * @return all task queues
   */
  public List<TaskQueue> listTaskQueues() {
    return taskQueueDAO.listAllTaskQueues();
  }
  
  /**
   * Updates a node that is responsible of the task queue
   * 
   * @param taskQueue queue name
   * @param responsibleNode node name
   * @return updated task queue
   */
  public TaskQueue updateTaskQueueResponsibleNode(TaskQueue taskQueue, String responsibleNode) {
    return taskQueueDAO.updateResponsibleNode(taskQueue, responsibleNode);
  }

  /**
   * Return whether node is responsible from queue
   * 
   * @param queueName queue name
   * @param responsibleNode node name
   * @return true if node is responsible from queue otherwise false
   */
  public boolean isNodeResponsibleFromQueue(String queueName, String responsibleNode) {
    TaskQueue taskQueue = taskQueueDAO.findByNameAndResponsibleNode(queueName, responsibleNode);
    return taskQueue != null;
  }
  
  /**
   * Return whether queue is empty
   * 
   * @param queueName queue name
   * @return true if queue is empty otherwise false
   */
  public boolean isQueueEmpty(String queueName) {
    TaskQueue taskQueue = taskQueueDAO.findByName(queueName);
    
    if (taskQueue == null) {
      return true;
    }
    
    return taskDAO.countByQueue(taskQueue) == 0;
  }
  
  /**
   * Return whether queue exists
   * 
   * @param queueName queue name
   * @return true if queue exists otherwise false
   */
  public boolean isQueueExisting(String queueName) {
    TaskQueue taskQueue = taskQueueDAO.findByName(queueName);
    return taskQueue != null;
  }
  
  @SuppressWarnings ("squid:S1168")
  private <T extends AbstractTask> byte[] serialize(T task) {
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

  private <T extends AbstractTask> void serializeToStream(T task, ByteArrayOutputStream resultStream) {
    try (ObjectOutputStream objectStream = new ObjectOutputStream(resultStream)) {
      objectStream.writeObject(task);
      objectStream.flush();
    } catch (IOException e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to serialize task", e);
      }
    }
  }

  private <T extends AbstractTask> T unserialize(byte[] rawData) {
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
  private <T extends AbstractTask> T unserializeFromStream(ByteArrayInputStream byteStream) {
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
