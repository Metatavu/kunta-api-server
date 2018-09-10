package fi.metatavu.kuntaapi.server.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.hibernate.JDBCException;

import fi.metatavu.kuntaapi.server.persistence.dao.TaskDAO;
import fi.metatavu.kuntaapi.server.persistence.dao.TaskQueueDAO;
import fi.metatavu.metaflow.tasks.Task;
import fi.metatavu.kuntaapi.server.persistence.model.TaskQueue;

@ApplicationScoped
public class TaskController {
  @Inject
  private Logger logger;

  @Inject
  private TaskQueueDAO taskQueueDAO;

  @Inject
  private TaskDAO taskDAO;
  
  @Inject
  private ClusterController clusterController;

  /**
   * Creates new task
   * 
   * @param <T> task type
   * @param queueName queue the task belongs to
   * @param task task data
   * @return created task entity
   */
  public <T extends Task> T createTask(String queueName, T task) {
    TaskQueue taskQueue = taskQueueDAO.findByName(queueName);
    if (taskQueue == null) {
      return null;
    }

    String uniqueId = task.getUniqueId();
    if (taskDAO.countByQueueAndUniqueId(taskQueue, uniqueId) == 0) {
      byte[] data = serialize(task);
	    if (data != null) {
	      return handleNewTask(queueName, task, taskQueue, uniqueId, data);
	    }
    } else {
      return handleExistingTask(queueName, task, uniqueId);
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
  public <T extends Task> T getNextTask(String queueName, String responsibleNode) {
    TaskQueue taskQueue = taskQueueDAO.findByNameAndResponsibleNode(queueName, responsibleNode);
    if (taskQueue == null) {
      return null;
    }
    
    fi.metatavu.kuntaapi.server.persistence.model.Task task = taskDAO.findNextInQueue(taskQueue);
    
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
   * Returns true if current queue is empty and local node is responsible from the queue
   * 
   * @param queueName Name of the queue
   * @return true if current queue is empty and local node is responsible from the queue
   */
  public boolean isEmptyAndLocalNodeResponsible(String queueName) {
    return isQueueEmpty(queueName) && isNodeResponsibleFromQueue(queueName, clusterController.getLocalNodeName());
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

  public <T extends Task> T findTask(String queueName, String uniqueId) {
    fi.metatavu.kuntaapi.server.persistence.model.Task task = findTaskModel(queueName, uniqueId);
    if (task == null) {
      return null;
    }

    return unserialize(task.getData());
  }
  
  /**
   * Creates new task queue
   * 
   * @param name queue name
   * @return created queue
   */
  public TaskQueue createTaskQueue(String name) {
    return taskQueueDAO.create(name, "UNKNOWN");
  }
  
  /**
   * Returns count of task queue
   * 
   * @return count of task queue
   */
  public Long countTaskQueues() {
    return taskQueueDAO.count();
  }
  
  /**
   * Returns task queue by index. List is ordered by id
   * 
   * @return task queue by index
   */
  public TaskQueue findTaskQueueByIndex(int index) {
    return taskQueueDAO.findTaskQueueByIndex(index);
  }

  private <T extends Task> T handleNewTask(String queueName, T task, TaskQueue taskQueue, String uniqueId, byte[] data) {
    if (task.getPriority()) {
      logger.log(Level.INFO, () -> String.format("Added priority task to the queue %s", queueName));
    }

    fi.metatavu.kuntaapi.server.persistence.model.Task newTask = createTaskModel(queueName, task.getPriority(), taskQueue, data, uniqueId);
    if (newTask != null) {
      return task;
    }

    logger.log(Level.WARNING, () -> String.format("Failed to add task %s to queue %s. Skipped", uniqueId, queueName));
    
    return null;
  }

  private <T extends Task> T handleExistingTask(String queueName, T task, String uniqueId) {
    if (task.getPriority()) {
      fi.metatavu.kuntaapi.server.persistence.model.Task prioritizedTask = prioritizeTaskModel(queueName, uniqueId);
      if (prioritizedTask != null) {
        return task;
      }

      logger.log(Level.WARNING, () -> String.format("Failed to prioritize task %s in queue %s. Skipped", uniqueId, queueName));
    } else {
      logger.log(Level.WARNING, () -> String.format("Task %s already found from queue %s. Skipped", uniqueId, queueName));
    }
    
    return null;
  }

  private fi.metatavu.kuntaapi.server.persistence.model.Task findTaskModel(String queueName, String uniqueId) {
    TaskQueue taskQueue = taskQueueDAO.findByName(queueName);
    if (taskQueue == null) {
      return null;
    }

    return taskDAO.findByQueueAndUniqueId(taskQueue, uniqueId);
  }
  
  private fi.metatavu.kuntaapi.server.persistence.model.Task prioritizeTaskModel(String queueName, String uniqueId) {
    fi.metatavu.kuntaapi.server.persistence.model.Task task = findTaskModel(queueName, uniqueId);
    if (task != null && !task.getPriority()) {
      return taskDAO.updatePriority(task, true);
    }
    
    logger.log(Level.WARNING, () -> String.format("Tried to prioritize already priority task %s from queue %s. Skipped", uniqueId, queueName));
    
    return null;
  }
  
  private fi.metatavu.kuntaapi.server.persistence.model.Task createTaskModel(String queueName, Boolean priority, TaskQueue taskQueue, byte[] data, String uniqueId) {
    try {
      return taskDAO.create(taskQueue, uniqueId, priority, data, OffsetDateTime.now());
    } catch (PersistenceException e) {
      handleCreateTaskErrorPersistence(queueName, uniqueId, e);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Task creating failed on unexpected error", e);
    }
    
    return null;
  }
  
  private void handleCreateTaskErrorPersistence(String queueName, String uniqueId, PersistenceException e) {
    if (e.getCause() instanceof JDBCException) {
      handleCreateTaskErrorJdbc(queueName, uniqueId, (JDBCException) e.getCause());
    } else {
      logger.log(Level.SEVERE, "Task creating failed on unexpected persistence error", e);
    }
  }

  private void handleCreateTaskErrorJdbc(String queueName, String uniqueId, JDBCException e) {
    if (e.getCause() instanceof SQLException) {
      SQLException sqlException = (SQLException) e.getCause();
      if (sqlException.getErrorCode() == 1062) {
        logger.log(Level.SEVERE,  () -> String.format("Task %s insterted twice into queue %s. Skipped", uniqueId, queueName));
        return;
      }
    }

    logger.log(Level.SEVERE, "Task creating failed on unexpected JDBC error", e);
  }
  
  @SuppressWarnings ("squid:S1168")
  private <T extends Task> byte[] serialize(T task) {
    try (ByteArrayOutputStream resultStream = new ByteArrayOutputStream()) {
      serializeToStream(task, resultStream);
      resultStream.flush();
      return resultStream.toByteArray();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to write serialized task data", e);
    }
    
    return null;
  }

  private <T extends Task> void serializeToStream(T task, ByteArrayOutputStream resultStream) {
    try (ObjectOutputStream objectStream = new ObjectOutputStream(resultStream)) {
      objectStream.writeObject(task);
      objectStream.flush();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to serialize task", e);
    }
  }

  private <T extends Task> T unserialize(byte[] rawData) {
    try (ByteArrayInputStream byteStream = new ByteArrayInputStream(rawData)) {
      return unserializeFromStream(byteStream);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to write unserialized task data", e);
    }
    
    return null;
  }

  @SuppressWarnings({"unchecked", "squid:S4508"})
  private <T extends Task> T unserializeFromStream(ByteArrayInputStream byteStream) {
    try (ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
      Object object = objectStream.readObject();
      if (object == null) {
        return null;
      }
      
      return (T) object;
    } catch (IOException | ClassNotFoundException e) {
      logger.log(Level.SEVERE, "Failed to unserialize task", e);
    }
    
    return null;
  }

}
