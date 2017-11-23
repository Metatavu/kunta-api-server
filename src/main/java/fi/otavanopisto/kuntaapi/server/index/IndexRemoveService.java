package fi.otavanopisto.kuntaapi.server.index;

public class IndexRemoveService implements IndexRemove {

  private String serviceId;

  @Override
  public String getId() {
    return serviceId;
  }

  @Override
  public String getType() {
    return "service";
  }
  
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }
  
}
