package fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.tasks.AbstractTaskQueue;

@ApplicationScoped
public class ServiceChannelTasksQueue extends AbstractTaskQueue<AbstractServiceChannelTask> {

  @Override
  public String getName() {
    return "service-channel-tasks";
  }
  
}