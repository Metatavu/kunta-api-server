package fi.metatavu.kuntaapi.server.integrations.ptv.tasks.lists;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvOrganizationListTask;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractKuntaApiTaskQueue;

/**
 * Task queue for priority PTV organization list tasks
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PriorityOrganizationListTaskQueue extends AbstractKuntaApiTaskQueue<PtvOrganizationListTask> {

  @Override
  public String getName() {
    return "ptv-priority-organization-list";
  }
  
}