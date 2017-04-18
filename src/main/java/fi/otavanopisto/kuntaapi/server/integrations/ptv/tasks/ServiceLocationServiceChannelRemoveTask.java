package fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks;

import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

public class ServiceLocationServiceChannelRemoveTask extends AbstractServiceChannelTask {

  private static final long serialVersionUID = 8278839832135682031L;

  private ServiceLocationServiceChannelId serviceLocationServiceChannelId;

  public ServiceLocationServiceChannelRemoveTask() {
    // Zero-argument constructor
  }

  public ServiceLocationServiceChannelRemoveTask(ServiceLocationServiceChannelId serviceLocationServiceChannelId) {
    super(Operation.REMOVE);
    this.serviceLocationServiceChannelId = serviceLocationServiceChannelId;
  }
  
  public ServiceLocationServiceChannelId getServiceLocationServiceChannelId() {
    return serviceLocationServiceChannelId;
  }
  
  public void setServiceLocationServiceChannelId(ServiceLocationServiceChannelId serviceLocationServiceChannelId) {
    this.serviceLocationServiceChannelId = serviceLocationServiceChannelId;
  }
  
}
