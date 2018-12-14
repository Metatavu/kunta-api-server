package fi.metatavu.kuntaapi.server.integrations.ptv.tasks.lists;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvServiceChannelListTask;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractKuntaApiTaskQueue;

/**
 * Task queue for non-priority PTV service channel list tasks
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ServiceChannelListTaskQueue extends AbstractKuntaApiTaskQueue<PtvServiceChannelListTask> {

  @Override
  public String getName() {
    return "ptv-service-channel-list";
  }
  
}