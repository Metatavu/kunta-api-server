package fi.otavanopisto.kuntaapi.server.tasks;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

/**
 * Abstract base class for organization entity update tasks
 * 
 * @author Antti Leppä
 *
 * @param <I> id type
 */
public abstract class AbstractOrganizationEntityTaskQueue extends AbstractTaskQueue<OrganizationEntityUpdateTask> {
  
  public abstract String getSource();
  public abstract String getEntity();
  
  @Override
  public String getName() {
    return String.format("%s-organization-%s-entities", getSource(), getEntity());
  }
  
  public void enqueueTask(OrganizationId organizationId) {
    enqueueTask(false, new OrganizationEntityUpdateTask(organizationId));
  }
  
  public void enqueueTasks(List<OrganizationId> organizationIds) {
    for (OrganizationId organizationId : organizationIds) {
      enqueueTask(organizationId);
    }
  }
  
}
