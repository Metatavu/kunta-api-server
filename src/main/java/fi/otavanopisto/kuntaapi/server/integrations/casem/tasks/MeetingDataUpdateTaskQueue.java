package fi.otavanopisto.kuntaapi.server.integrations.casem.tasks;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import fi.otavanopisto.kuntaapi.server.tasks.AbstractTaskQueue;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
public class MeetingDataUpdateTaskQueue extends AbstractTaskQueue<MeetingDataUpdateTask> {

  @Override
  public String getName() {
    return "casem-meeting-data";
  }
  
  @SuppressWarnings("unchecked")
  public void onTaskRequest(@Observes TaskRequest request) {
    if (request.getTask() instanceof MeetingDataUpdateTask) {
      enqueueTask(request.isPriority(), (MeetingDataUpdateTask) request.getTask());
    }
  }
  
}