package fi.metatavu.kuntaapi.server.tasks.metaflow;

import java.util.List;

import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.tasks.ServiceEntityUpdateTask;

/**
 * Abstract base class for service entity update tasks
 * 
 * @author Antti Leppä
 *
 * @param <I> id type
 */
public abstract class AbstractServiceEntityTaskQueue extends AbstractKuntaApiTaskQueue<ServiceEntityUpdateTask> {
  
  public abstract String getSource();
  public abstract String getEntity();
  
  @Override
  public String getName() {
    return String.format("%s-service-%s-entities", getSource(), getEntity());
  }
  
  public void enqueueTask(boolean priority, ServiceId serviceId) {
    enqueueTask(new ServiceEntityUpdateTask(priority, serviceId));
  }
  
  public void enqueueTasks(List<ServiceId> serviceIds) {
    for (ServiceId serviceId : serviceIds) {
      enqueueTask(false, serviceId);
    }
  }
  
}
