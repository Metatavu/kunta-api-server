package fi.otavanopisto.kuntaapi.server.index;

public class IndexRemoveElectronicServiceChannel implements IndexRemove {

  private String serviceChannelId;

  @Override
  public String getId() {
    return getServiceChannelId();
  }

  @Override
  public String getType() {
    return "electronic-service-channel";
  }
  
  public String getServiceChannelId() {
    return serviceChannelId;
  }
  
  public void setServiceChannelId(String serviceChannelId) {
    this.serviceChannelId = serviceChannelId;
  }
  
}
