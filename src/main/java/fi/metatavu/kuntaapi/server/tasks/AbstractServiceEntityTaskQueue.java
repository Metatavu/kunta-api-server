package fi.metatavu.kuntaapi.server.tasks;

import java.util.List;

import fi.metatavu.kuntaapi.server.id.ServiceId;

/**
 * Abstract base class for service entity update tasks
 * 
 * @author Antti Leppä
 *
 * @param <I> id type
 */
public abstract class AbstractServiceEntityTaskQueue extends AbstractTaskQueue<ServiceEntityUpdateTask> {
  
  public abstract String getSource();
  public abstract String getEntity();
  
  @Override
  public String getName() {
    return String.format("%s-service-%s-entities", getSource(), getEntity());
  }
  
  public void enqueueTask(ServiceId serviceId) {
    enqueueTask(false, new ServiceEntityUpdateTask(serviceId));
  }
  
  public void enqueueTasks(List<ServiceId> serviceIds) {
    for (ServiceId serviceId : serviceIds) {
      enqueueTask(serviceId);
    }
  }
  
}
