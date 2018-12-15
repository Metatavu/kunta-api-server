package fi.metatavu.kuntaapi.server.integrations.ptv.tasks.lists;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvServiceChannelListTask;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractKuntaApiTaskQueue;

/**
 * Task queue for priority PTV service channel list tasks
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PriorityServiceChannelListTaskQueue extends AbstractKuntaApiTaskQueue<PtvServiceChannelListTask> {

  @Override
  public String getName() {
    return "ptv-priority-service-channel-list";
  }
  
}