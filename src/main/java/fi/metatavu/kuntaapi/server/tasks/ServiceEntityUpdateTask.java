package fi.metatavu.kuntaapi.server.tasks;

import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.metaflow.tasks.impl.DefaultTaskImpl;

/**
 * Service entity update task
 * 
 * @author Antti Leppä
 *
 */
public class ServiceEntityUpdateTask extends DefaultTaskImpl {
  
  private static final long serialVersionUID = 6512130266904607810L;
  
  private ServiceId serviceId;
  
  /**
   * Zero-argument constructor
   */
  public ServiceEntityUpdateTask() {
    // Zero-argument constructor
  }
  
  /**
   * Constructor
   * 
   * @param priority priority
   * @param serviceId organization id
   */
  public ServiceEntityUpdateTask(boolean priority, ServiceId serviceId) {
    super(String.format("ptv-service-entity-update-task-%s", serviceId.toString()), priority);
    this.serviceId = serviceId;
  }

  public ServiceId getServiceId() {
    return serviceId;
  }
  
  public void setServiceId(ServiceId serviceId) {
    this.serviceId = serviceId;
  }
  
}
