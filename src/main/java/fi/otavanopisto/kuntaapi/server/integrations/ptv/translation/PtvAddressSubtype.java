package fi.otavanopisto.kuntaapi.server.integrations.ptv.translation;

public enum PtvAddressSubtype {

  UNKNOWN (""),
  NO_ADDRESS ("NoAddress"),
  STREET ("Street"),
  POST_OFFICE_BOX  ("PostOfficeBox"),
  SINGLE ("Single"),
  ABROAD ("Abroad"),
  MULTIPOINT ("Multipoint");
  
  private String ptvValue;
  
  private PtvAddressSubtype(String ptvValue) {
    this.ptvValue = ptvValue;
  }
  
  public String getPtvValue() {
    return ptvValue;
  }
  
}
