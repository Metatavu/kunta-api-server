package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.AbstractKuntaApiTaskQueue;

@ApplicationScoped
public class ServiceChannelTasksQueue extends AbstractKuntaApiTaskQueue<AbstractServiceChannelTask> {

  @Override
  public String getName() {
    return "service-channel-tasks";
  }
  
}