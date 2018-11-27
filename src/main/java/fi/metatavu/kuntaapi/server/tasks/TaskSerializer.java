package fi.metatavu.kuntaapi.server.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaflow.tasks.Task;

/**
 * Task serializer
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TaskSerializer {
  
  @Inject
  private Logger logger;

  /**
   * Serializes task into byte array
   * 
   * @param task task
   * @return serialized task
   */
  @SuppressWarnings ("squid:S1168")
  public <T extends Task> byte[] serialize(T task) {
    try (ByteArrayOutputStream resultStream = new ByteArrayOutputStream()) {
      serializeToStream(task, resultStream);
      resultStream.flush();
      return resultStream.toByteArray();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to write serialized task data", e);
    }
    
    return null;
  }

  /**
   * Unserializes task from byte array
   * 
   * @param rawData serialized data
   * @return unserialized task or null if unserialization has failed
   */
  public <T extends Task> T unserialize(byte[] rawData) {
    try (ByteArrayInputStream byteStream = new ByteArrayInputStream(rawData)) {
      return unserializeFromStream(byteStream);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to write unserialized task data", e);
    }
    
    return null;
  }

  /**
   * Serializes a task into stream
   * 
   * @param task task
   * @param stream stream
   */
  private <T extends Task> void serializeToStream(T task, OutputStream stream) {
    try (ObjectOutputStream objectStream = new ObjectOutputStream(stream)) {
      objectStream.writeObject(task);
      objectStream.flush();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to serialize task", e);
    }
  }

  /**
   * Unserializes task from stream
   * 
   * @param stream stream
   * @return unserialized data
   */
  @SuppressWarnings({"unchecked", "squid:S4508"})
  private <T extends Task> T unserializeFromStream(InputStream stream) {
    try (ObjectInputStream objectStream = new ObjectInputStream(stream)) {
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
