package fi.otavanopisto.kuntaapi.server.index;

public class IndexRemoveService implements IndexRemove {

  private String serviceId;
  private String language;

  @Override
  public String getId() {
    return String.format("%s_%s", serviceId, language);
  }

  @Override
  public String getType() {
    return "service";
  }
  
  public String getLanguage() {
    return language;
  }
  
  public void setLanguage(String language) {
    this.language = language;
  }
  
  public String getServiceId() {
    return serviceId;
  }
  
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }
  
}
