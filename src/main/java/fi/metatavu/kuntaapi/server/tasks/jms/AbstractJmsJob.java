package fi.metatavu.kuntaapi.server.tasks.jms;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
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
  
  @Inject
  private SystemSettingController systemSettingController;

  @Resource (lookup = JmsQueueProperties.CONNECTION_FACTORY)
  private ConnectionFactory connectionFactory;
  
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
    if (!systemSettingController.isNotTestingOrTestRunning()) {
      return;
    }
    
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
          Serializable response = executeInternal(task);
          if (streamMessage.getJMSReplyTo() != null) {
            sendReplyMessage(streamMessage.getJMSReplyTo(), response);
          } 
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
  
  /**
   * Actually executes scheduled job. 
   * 
   * @param task task
   * @return response object
   */
  protected Serializable executeInternal(T task) {
    execute(task);
    return Boolean.TRUE;
  }
  
  /**
   * Sends reply to message
   * 
   * @param replyTo reply destination
   * @param reply reply object 
   * @throws JMSException thrown when message sending fails
   */
  protected <R extends Serializable> void sendReplyMessage(Destination replyTo, R reply) throws JMSException {
    try (Connection connection = connectionFactory.createConnection()) {
      try (Session session = createSession(connection)) {
        try (MessageProducer producer = session.createProducer(replyTo)) {
          ObjectMessage replyMessage = session.createObjectMessage(reply);
          replyMessage.setStringProperty(JmsQueueProperties.MESSAGE_TYPE, JmsQueueProperties.MESSAGE_TYPE_REPLY);
          producer.send(replyMessage, DeliveryMode.PERSISTENT, 9, Message.DEFAULT_TIME_TO_LIVE);
        }
      }
    }
  }
  
  /**
   * Creates new JMS session
   * 
   * @return new JMS session
   * @throws JMSException thrown when session creation fails
   */
  private Session createSession(Connection connection) throws JMSException {
    return connection.createSession();
  }
}
