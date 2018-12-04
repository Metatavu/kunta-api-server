package fi.metatavu.kuntaapi.server.tasks.jms;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
  
  @Resource (lookup = JmsQueueProperties.CONNECTION_FACTORY)
  private ConnectionFactory connectionFactory;
  
  /**
   * Enqueues task into the queue. 
   * 
   * Method does not wait for task completion
   * 
   * @param task task
   */
  public void enqueueTask(T task) {
    try {
      Session session = createSession();
      StreamMessage message = createMessage(session, task);
      if (message != null) {
        MessageProducer producer = createProducer(session);
        producer.send(message);
      }
    } catch (NamingException | JMSException e) {
      logger.log(Level.SEVERE, "Failed to enqueue task", e);
    }
  }
  
  /**
   * Enqueues task into the queue and returns future task for completion
   * 
   * @param task task
   * @return future task for completion
   */
  public Future<Task> enqueueTaskAsync(T task) {
    try {
      Session session = createSession();
      StreamMessage message = createMessage(session, task);
      if (message != null) {
        MessageProducer producer = createProducer(session);
        TaskCompletionFuture result = new TaskCompletionFuture(task);
        producer.send(message, new TaskCompletionListener(result));
        return result;
      }
    } catch (NamingException | JMSException e) {
      logger.log(Level.SEVERE, "Failed to enqueue task", e);
    }
    
    return null;
  }
  
  /**
   * Enqueues task into the queue and waits for it's completion
   * 
   * @param task task
   * @throws ExecutionException when task execution fails
   * @throws InterruptedException when execution is interrupted
   */
  public void enqueueTaskSync(T task) throws InterruptedException, ExecutionException {
    enqueueTaskAsync(task).get();    
  }

  /**
   * Creates JMS message producer
   * 
   * @param session session
   * @return JMS message producer
   * @throws JMSException when producer creation fails
   * @throws NamingException thrown when JNDI lookup fails
   */
  private MessageProducer createProducer(Session session) throws JMSException, NamingException {
    MessageProducer producer = session.createProducer(getQueue());
    return producer;
  }

  /**
   * Creates new JMS session
   * 
   * @return new JMS session
   * @throws JMSException thrown when session creation fails
   */
  private Session createSession() throws JMSException {
    Connection connection = connectionFactory.createConnection();
    return connection.createSession();
  }
  
  /**
   * Creates task message
   * 
   * @param session session
   * @param task task
   * @return message
   * @throws JMSException thrown on JMS exception
   * @throws NamingException thrown when JNDI lookup fails
   */
  private StreamMessage createMessage(Session session, T task) throws JMSException, NamingException {
    StreamMessage message = session.createStreamMessage();
    byte[] taskData = taskSerializer.serialize(task);
    if (taskData != null) {
      message.writeBytes(taskData);
      return message;
    } else {
      logger.severe("Failed to serialize task");
    }

    return null;
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