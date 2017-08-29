package fi.otavanopisto.kuntaapi.server.tasks;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.ClusterController;
import fi.otavanopisto.kuntaapi.server.controllers.TaskController;
import fi.otavanopisto.kuntaapi.server.persistence.model.TaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Timer;

@Startup
@Singleton
@ApplicationScoped
public class TaskQueueDistributor {

  private static final int UPDATE_INTERVAL = 1000 * 60 * 5;
  
  @Inject
  private Logger logger;
  
  @Inject
  private TaskController taskController;
  
  @Inject
  private ClusterController clusterController;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Resource
  private TimerService timerService;

  @PostConstruct
  public void postConstruct() {
    startTimer();
  }
  
  @Timeout
  public void onTimeout() {
    if (!systemSettingController.inFailsafeMode()) {
      selfAssignQueues();
    }
  }
  
  private void selfAssignQueues() {
    try {
      String localNodeName = clusterController.getLocalNodeName();
      List<String> nodeNames = clusterController.getNodeNames();
      int myIndex = nodeNames.indexOf(localNodeName);
      int nodeCount = nodeNames.size();
      List<TaskQueue> taskQueues = taskController.listTaskQueues();
      
      for (int i = 0; i < taskQueues.size(); i++) {
        if ((i % nodeCount) == myIndex) {
          taskController.updateTaskQueueResponsibleNode(taskQueues.get(i), localNodeName);
        }
      }
    } finally {
      try {
        Iterator<Timer> timers = timerService.getTimers().iterator();
        while(timers.hasNext()) {
          Timer timer = timers.next();
          timer.cancel();
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Exception while canceling timer", e);
      } finally {
        startTimer();
      }
    }
  }
  
  private void startTimer() {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(systemSettingController.inTestMode() ? 1000 : UPDATE_INTERVAL, timerConfig);
  }
  
}
