package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvServiceListTask;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractKuntaApiTaskQueue;

/**
 * Task queue for non-priority PTV service list tasks
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ServiceListTaskQueue extends AbstractKuntaApiTaskQueue<PtvServiceListTask> {

  @Override
  public String getName() {
    return "ptv-service-list";
  }
  
}