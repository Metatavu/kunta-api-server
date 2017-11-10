package fi.otavanopisto.kuntaapi.server.integrations.ptv;

public enum CodeType {
  
  MUNICIPALITY ("Municipality"), 
  
  PROVINCE ("Province"), 
  
  HOSPITAL_REGIONS ("HospitalRegions"), 
  
  BUSINESS_REGIONS ("BusinessRegions"), 
  
  COUNTRY ("Country"), 
  
  LANGUAGE ("Language"), 
  
  POSTAL ("Postal");
  
  private String type;
  
  private CodeType(String type) {
    this.type = type;
  }
  
  public String getType() {
    return type;
  }
  
}
