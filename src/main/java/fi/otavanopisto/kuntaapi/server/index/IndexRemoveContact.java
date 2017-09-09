package fi.otavanopisto.kuntaapi.server.index;

public class IndexRemoveContact implements IndexRemove {

  private String contactId;

  @Override
  public String getId() {
    return contactId;
  }

  @Override
  public String getType() {
    return "contact";
  }
  
  public void setContactId(String contactId) {
    this.contactId = contactId;
  }
  
  public String getContactId() {
    return contactId;
  }

}
