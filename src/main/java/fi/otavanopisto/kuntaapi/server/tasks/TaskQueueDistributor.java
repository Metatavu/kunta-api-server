package fi.otavanopisto.kuntaapi.server.tasks;

import java.util.List;
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
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.commons.codec.binary.StringUtils;

import fi.otavanopisto.kuntaapi.server.controllers.ClusterController;
import fi.otavanopisto.kuntaapi.server.controllers.TaskController;
import fi.otavanopisto.kuntaapi.server.persistence.model.TaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@Startup
@Singleton
@ApplicationScoped
public class TaskQueueDistributor {

  private static final int UPDATE_INTERVAL = 1000 * 60;
  private static final int TEST_UPDATE_INTERVAL = 1000;
  
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

  @PostConstruct
  public void postConstruct() {
    startTimer();
  }
  
  private void selfAssignQueues() {
    String localNodeName = clusterController.getLocalNodeName();
    List<String> nodeNames = clusterController.getNodeNames();
    int myIndex = nodeNames.indexOf(localNodeName);
    int nodeCount = nodeNames.size();
    List<TaskQueue> taskQueues = taskController.listTaskQueues();
    
    logger.log(Level.INFO, () -> String.format("Reassigning queues, found %d workers online. My index is %d", nodeCount, myIndex));
    
    for (int i = 0; i < taskQueues.size(); i++) {
      if ((i % nodeCount) == myIndex) {
        TaskQueue taskQueue = taskQueues.get(i);
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
    managedScheduledExecutorService.scheduleWithFixedDelay(() -> {
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
