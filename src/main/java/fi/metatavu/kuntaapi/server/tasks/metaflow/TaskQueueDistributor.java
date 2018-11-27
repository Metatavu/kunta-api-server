package fi.metatavu.kuntaapi.server.tasks.metaflow;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.commons.codec.binary.StringUtils;

import fi.metatavu.kuntaapi.server.controllers.ClusterController;
import fi.metatavu.kuntaapi.server.controllers.TaskController;
import fi.metatavu.kuntaapi.server.lifecycle.BeforeShutdownEvent;
import fi.metatavu.kuntaapi.server.persistence.model.TaskQueue;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;

@Startup
@Singleton
@ApplicationScoped
public class TaskQueueDistributor {

  private static final int UPDATE_INTERVAL = 1000 * 60;
  private static final int TEST_UPDATE_INTERVAL = 5000;
  
  @Inject
  private Logger logger;
  
  @Inject
  private TaskController taskController;
  
  @Inject
  private ClusterController clusterController;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Resource
  private ManagedScheduledExecutorService managedScheduledExecutorService;
  
  @Resource
  private EJBContext ejbContext;
  
  private ScheduledFuture<?> future;

  @PostConstruct
  public void postConstruct() {
    startTimer();
  }
  
  /**
   * Event listener that listens server shutdown event
   * 
   * @param event event
   */
  public void onBeforeShutdown(@Observes BeforeShutdownEvent event) {
    logger.info("Cancelling task");

    if (future != null) {
      future.cancel(false);
    }

    logger.info("Task cancelled");
  }
  
  private void selfAssignQueues() {
    String localNodeName = clusterController.getLocalNodeName();
    List<String> nodeNames = clusterController.getNodeNames();
    int myIndex = nodeNames.indexOf(localNodeName);
    int nodeCount = nodeNames.size();
    Long queueCount = taskController.countTaskQueues();
    logger.log(Level.INFO, () -> String.format("Reassigning queues, found %d workers online. My index is %d", nodeCount, myIndex));
    
    for (int i = 0; i < queueCount; i++) {
      if ((i % nodeCount) == myIndex) {
        TaskQueue taskQueue = taskController.findTaskQueueByIndex(i);
        if (!StringUtils.equals(taskQueue.getResponsibleNode(), localNodeName)) {
          logger.log(Level.INFO, () -> String.format("Worker %s reserved queue %s", localNodeName, taskQueue.getName()));
          taskController.updateTaskQueueResponsibleNode(taskQueue, localNodeName);
        }
      }
    }
  }
  
  private void startTimer() {
    boolean testMode = systemSettingController.inTestMode();
    long interval = testMode ? TEST_UPDATE_INTERVAL : UPDATE_INTERVAL;
    startTimer(interval, interval);
  }
  
  private void startTimer(long warmup, long delay) {
    future = managedScheduledExecutorService.scheduleWithFixedDelay(() -> {
      UserTransaction userTransaction = ejbContext.getUserTransaction();
      try {
        userTransaction.begin();
        
        if (!systemSettingController.inFailsafeMode()) {
          selfAssignQueues();
        }
        
        userTransaction.commit();
      } catch (Exception ex) {
        logger.log(Level.SEVERE, "TaskQueueDistributor crashed", ex);
        try {
          if (userTransaction != null) {
            userTransaction.rollback();
          }
        } catch (SystemException e1) {
          logger.log(Level.SEVERE, "Failed to rollback transaction", e1);
        }
      }
    }, warmup, delay, TimeUnit.MILLISECONDS);
  }
  
}
