package fi.otavanopisto.kuntaapi.server.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.OffsetDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.persistence.dao.TaskDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.Task;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTask;

@ApplicationScoped
public class TaskController {
  
  @Inject
  private Logger logger;
  
  @Inject
  private TaskDAO taskDAO;

  /**
   * Creates new task
   * 
   * @param queue queue the task belongs to
   * @param priority whether the task is a priority task or not
   * @param task task data
   * @return created task entity
   */
  public <T extends AbstractTask> Task createTask(String queue, Boolean priority, T task) {
    byte[] data = serialize(task);
    return taskDAO.create(queue, priority, data, OffsetDateTime.now());
  }
  
  /**
   * Returns next tasks in queue
   * 
   * @param queue queue
   * @return next tasks in queue
   */
  public <T extends AbstractTask> T getNextTask(String queue) {
    T result = null;
    Task task = taskDAO.findByNextInQueue(queue);
    
    if (task != null) {
      result = unserialize(task.getData());
      taskDAO.delete(task);
    }
    
    return result;
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
