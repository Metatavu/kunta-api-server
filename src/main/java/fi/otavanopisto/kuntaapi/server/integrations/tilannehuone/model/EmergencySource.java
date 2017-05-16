package fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.model;

import java.io.Serializable;

public class EmergencySource implements Serializable {

  private static final long serialVersionUID = 8858699590999705421L;

  private String url;
  private String name;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getUrl() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
}
