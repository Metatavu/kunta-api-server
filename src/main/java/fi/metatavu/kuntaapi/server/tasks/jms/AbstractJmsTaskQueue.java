package fi.metatavu.kuntaapi.server.tasks.jms;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import fi.metatavu.kuntaapi.server.tasks.TaskSerializer;
import fi.metatavu.metaflow.tasks.Task;

/**
 * Abstract base class for all JMS based task queues
 * 
 * @author Antti Leppä
 *
 * @param <T> task
 */
public abstract class AbstractJmsTaskQueue<T extends Task> {
  
  protected final static String JMS_QUEUE_PREFIX = "java:/jms/queue/";
  
  @Inject
  private Logger logger;

  @Inject
  private TaskSerializer taskSerializer; 
  
  @Resource (lookup = "java:/ConnectionFactory")
  private ConnectionFactory connectionFactory;
  
  /**
   * Enqueues task into the queue
   * 
   * @param task task
   */
  public void enqueueTask(T task) {
    try {
      Queue queue = getQueue();
      Connection connection = connectionFactory.createConnection();
      Session session = connection.createSession();
      MessageProducer producer = session.createProducer(queue);
      
      StreamMessage message = session.createStreamMessage();
      byte[] taskData = taskSerializer.serialize(task);
      if (taskData != null) {
        message.writeBytes(taskData);
        producer.send(message);
      } else {
        logger.severe("Failed to serialize task");
      }
    } catch (NamingException | JMSException e) {
      logger.log(Level.SEVERE, "Failed to enqueue task", e);
    }
  }
  
  /**
   * Returns queues's name
   * 
   * @return queues's name
   */
  protected abstract String getName();
  
  /**
   * Returns JMS queue
   * 
   * @return JMS queue
   * @throws NamingException thrown when lookup fails
   */
  private Queue getQueue() throws NamingException {
    return (Queue) (new InitialContext()).lookup(String.format("java:/jms/queue/%s", getName()));
  }

}