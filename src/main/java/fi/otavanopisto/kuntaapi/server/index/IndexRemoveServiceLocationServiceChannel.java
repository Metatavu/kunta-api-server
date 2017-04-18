package fi.otavanopisto.kuntaapi.server.index;

public class IndexRemoveServiceLocationServiceChannel implements IndexRemove {

  private String serviceLocationServiceChannelId;
  
  private String language;

  @Override
  public String getId() {
    return String.format("%s_%s", serviceLocationServiceChannelId, language);
  }

  @Override
  public String getType() {
    return "service-location-service-channel";
  }
  
  public String getLanguage() {
    return language;
  }
  
  public void setLanguage(String language) {
    this.language = language;
  }
  
  public String getServiceLocationServiceChannelId() {
    return serviceLocationServiceChannelId;
  }
  
  public void setServiceLocationServiceChannelId(String serviceLocationServiceChannelId) {
    this.serviceLocationServiceChannelId = serviceLocationServiceChannelId;
  }
  
}
