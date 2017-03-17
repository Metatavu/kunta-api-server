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

@Startup
@Singleton
@ApplicationScoped
public class TaskQueueDistributor {

  private static final int UPDATE_INTERVAL = 60000;
  
  @Inject
  private TaskController taskController;
  
  @Inject
  private ClusterController clusterController;

  @Resource
  private TimerService timerService;

  @PostConstruct
  public void postConstruct() {
    selfAssignQueues();
  }
  
  @Timeout
  public void onTimeout() {
    selfAssignQueues();
  }
  
  private void selfAssignQueues() {
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
    
    startTimer();
  }
  
  private void startTimer() {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(UPDATE_INTERVAL, timerConfig);
  }
  
}
