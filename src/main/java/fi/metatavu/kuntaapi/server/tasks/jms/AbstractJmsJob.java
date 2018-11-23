package fi.metatavu.kuntaapi.server.tasks.jms;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.StreamMessage;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.kuntaapi.server.tasks.TaskSerializer;
import fi.metatavu.metaflow.tasks.Task;

/**
 * Abstract base class for JMS jobs
 * 
 * @author Antti Leppä
 *
 * @param <T> task
 */
public abstract class AbstractJmsJob <T extends Task> implements MessageListener {
  
  private static final int BUFFER_SIZE = 1024;

  @Inject
  private Logger logger;

  @Inject
  private TaskSerializer taskSerializer;
  
  /**
   * Calculates md5 hash from entity
   * 
   * @param entity entity
   * @return entity md5 hash
   */
  protected String createPojoHash(Object entity) {
    try {
      return DigestUtils.md5Hex(new ObjectMapper().writeValueAsBytes(entity));
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to create hash", e);
    }
    
    return null;
  }

  @Override
  public void onMessage(Message message) {
    if (message instanceof StreamMessage) {
      StreamMessage streamMessage = (StreamMessage) message;
      ByteArrayOutputStream messageStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[BUFFER_SIZE];
      try {
        do {
          int bytesRead = streamMessage.readBytes(buffer);
          messageStream.write(buffer, 0, bytesRead);
          if (bytesRead < BUFFER_SIZE) {
            break;
          }
        } while (true);
        
        T task = taskSerializer.unserialize(messageStream.toByteArray());
        if (task != null) {
          execute(task);
        } else {
          logger.severe("Failed to unserialize task");          
        }
      } catch (JMSException e) {
        logger.severe("Failed to receive task from JMS queue");   
      }
    }
  }
  
  /**
   * Executes scheduled job 
   */
  public abstract void execute(T task);
  
}
