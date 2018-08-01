package fi.metatavu.kuntaapi.server.tasks;

import fi.metatavu.kuntaapi.server.id.ServiceId;

/**
 * Service entity update task
 * 
 * @author Antti Leppä
 *
 */
public class ServiceEntityUpdateTask extends AbstractTask {
  
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
   * @param serviceId organization id
   */
  public ServiceEntityUpdateTask(ServiceId serviceId) {
    super();
    this.serviceId = serviceId;
  }

  public ServiceId getServiceId() {
    return serviceId;
  }
  
  public void setServiceId(ServiceId serviceId) {
    this.serviceId = serviceId;
  }

  @Override
  public String getUniqueId() {
    return String.format("ptv-service-entity-update-task-%s", serviceId.toString());
  }
  
}
