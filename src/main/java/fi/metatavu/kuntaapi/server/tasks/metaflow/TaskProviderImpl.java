package fi.metatavu.kuntaapi.server.tasks.metaflow;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.ClusterController;
import fi.metatavu.kuntaapi.server.controllers.TaskController;
import fi.metatavu.metaflow.tasks.Task;
import fi.metatavu.metaflow.tasks.TaskProvider;

public class TaskProviderImpl implements TaskProvider {
  
  @Inject
  private ClusterController clusterController;

  @Inject
  private TaskController taskController;

  @Override
  public <T extends Task> T getNextTask(String queueName) {
    return taskController.getNextTask(queueName, clusterController.getLocalNodeName());
  }

  @Override
  public <T extends Task> T createTask(String queueName, T task) {
    taskController.createTask(queueName, task);
    return task;
  }

  @Override
  public boolean isQueueEmpty(String queueName) {
    return taskController.isQueueEmpty(queueName);
  }

  @Override
  public <T extends Task> T findTask(String queueName, String uniqueId) {
    return taskController.findTask(queueName, uniqueId);
  }

  @Override
  public void prepareQueue(String queueName) {
    if (!taskController.isQueueExisting(queueName)) {
      taskController.createTaskQueue(queueName);
    }
  }
}