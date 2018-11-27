package fi.metatavu.kuntaapi.server.tasks.metaflow;

import java.util.List;

import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.tasks.OrganizationEntityUpdateTask;

/**
 * Abstract base class for organization entity update tasks
 * 
 * @author Antti Leppä
 *
 * @param <I> id type
 */
public abstract class AbstractOrganizationEntityTaskQueue extends AbstractKuntaApiTaskQueue<OrganizationEntityUpdateTask> {
  
  public abstract String getSource();
  public abstract String getEntity();
  
  
  @Override
  public String getName() {
    return String.format("%s-organization-%s-entities", getSource(), getEntity());
  }

  public void enqueueTask(OrganizationId organizationId, int offset) {
    enqueueTask(new OrganizationEntityUpdateTask(organizationId, offset));
  }
  
  public void enqueueTask(OrganizationId organizationId) {
    enqueueTask(organizationId, 0);
  }
  
  public void enqueueTasks(List<OrganizationId> organizationIds) {
    for (OrganizationId organizationId : organizationIds) {
      enqueueTask(organizationId);
    }
  }
  
}
