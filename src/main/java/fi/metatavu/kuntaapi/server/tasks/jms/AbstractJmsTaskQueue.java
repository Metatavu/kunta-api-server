package fi.metatavu.kuntaapi.server.tasks.jms;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import fi.metatavu.kuntaapi.server.locking.ClusterLockController;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.TaskSerializer;
import fi.metatavu.metaflow.tasks.Task;

/**
 * Abstract base class for all JMS based task queues
 * 
 * @author Antti Leppä
 *
 * @param <T> task
 */
public abstract class AbstractJmsTaskQueue<T extends Task, R extends Serializable> {
  
  protected final static String JMS_QUEUE_PREFIX = "java:/jms/queue/";
  
  private static final int HIGH_PRIORITY = 9;
  private static final int DEFAULT_PRIORITY = Message.DEFAULT_PRIORITY;
  private static final long REPLY_TIMEOUT = 1000;
  private static final long MAX_REPLY_TIMEOUT = 2 * 60 * 1000;
  
  @Inject
  private Logger logger;

  @Inject
  private TaskSerializer taskSerializer; 

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private ClusterLockController clusterLockController;
  
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
    enqueueTask(task, 0, false);
  }
  
  /**
   * Enqueues task into the queue. 
   * 
   * Method does not wait for task completion
   * 
   * @param task task
   * @param deliveryDelay defines minimum length of time in milliseconds before the task is delivered into the queue. Zero means that task is queued immediately
   */
  public void enqueueTask(T task, int deliveryDelay) {
    enqueueTask(task, deliveryDelay, false);
  }

  /**
   * Enqueues task into the queue. 
   * 
   * Method waits for task completion
   * 
   * @param task task
   */
  public R enqueueTaskSync(T task) {
    return enqueueTask(task, 0, true);
  }
  
  /**
   * Enqueues task into the queue. 
   * 
   * @param task task
   * @param deliveryDelay defines minimum length of time in milliseconds before the task is delivered into the queue. Zero means that task is queued immediately
   * @param blocking Whether to block until a reply message arrives
   */
  @SuppressWarnings("unchecked")
  @Transactional (value = TxType.REQUIRES_NEW)
  public R enqueueTask(T task, int deliveryDelay, boolean blocking) {
    String lockKey = String.format("task-%s", task.getUniqueId());
    
    if (!clusterLockController.lockUntilTransactionCompletion(lockKey)) {
      if (logger.isLoggable(Level.FINEST)) {
        logger.log(Level.FINEST, String.format("Reroll task %s", lockKey));
      }
      
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        if (logger.isLoggable(Level.WARNING)) {
          logger.log(Level.WARNING, "Lock reroll cooldown interrupted", e);
        }
      }
      
      return enqueueTask(task, deliveryDelay, blocking);
    } else if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, String.format("Locked task %s", lockKey));
    }
    
    if (!systemSettingController.isNotTestingOrTestRunning()) {
      return null;
    }
    
    try (Connection connection = connectionFactory.createConnection()) {
      connection.start();
      
      try (Session session = createSession(connection)) {
        StreamMessage message = createMessage(session, task);
        if (message != null) {
          Queue queue = getQueue();
          
          try (MessageProducer producer = createProducer(queue, session)) {
            if (task.getPriority()) {
              logger.log(Level.INFO, () -> String.format("Added priority task to the JMS queue %s", getName()));
            }
            
            message.setStringProperty(JmsQueueProperties.MESSAGE_TYPE, JmsQueueProperties.MESSAGE_TYPE_TASK);
            
            if (blocking) {
              message.setJMSReplyTo(queue);
            }
            
            try (MessageConsumer consumer = createConsumer(queue, session, JmsQueueProperties.REPLY_MESSAGE_SELECTOR)) {
              if (deliveryDelay > 0) {
                producer.setDeliveryDelay(deliveryDelay);
              }
              
              producer.send(message, DeliveryMode.PERSISTENT, task.getPriority() ? HIGH_PRIORITY : DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
              
              if (blocking) {
                long waitTimeout = System.currentTimeMillis() + MAX_REPLY_TIMEOUT;
                while (true) {
                  Message replyMessage = consumer.receive(REPLY_TIMEOUT);
                  if (replyMessage != null) {
                    if (replyMessage instanceof ObjectMessage) {
                      return (R) ((ObjectMessage) replyMessage).getObject();
                    } else if (replyMessage instanceof TextMessage) {
                      logger.severe(String.format("Received text message with message %s instead of object message", ((TextMessage) replyMessage).getText()));
                      break;
                    } else {
                      logger.severe(String.format("Received %s message instead of object message", message.toString()));
                      break;                      
                    }
                  }
                  
                  if (System.currentTimeMillis() > waitTimeout) {
                    if (logger.isLoggable(Level.SEVERE)) {
                      logger.severe(String.format("Waiting for blocking task in queue %s timed out", getName()));
                    }
                    
                    break;
                  }
                }
              }
            }
          }
        }
      } finally {
        if (connection != null) { 
          connection.close();
        }
      }
      
    } catch (NamingException | JMSException e) {
      logger.log(Level.SEVERE, "Failed to enqueue task", e);
    }
    
    return null;
  }

  /**
   * Creates JMS message producer
   * 
   * @param session session
   * @return JMS message producer
   * @throws JMSException when producer creation fails
   * @throws NamingException thrown when JNDI lookup fails
   */
  private MessageProducer createProducer(Queue queue, Session session) throws JMSException, NamingException {
    MessageProducer producer = session.createProducer(queue);
    return producer;
  }

  private MessageConsumer createConsumer(Queue queue, Session session, String selector) throws JMSException {
    return session.createConsumer(queue, selector);
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