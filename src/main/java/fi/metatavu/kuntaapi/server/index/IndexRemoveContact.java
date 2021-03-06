package fi.metatavu.kuntaapi.server.index;

public class IndexRemoveContact implements IndexRemove {

  private String contactId;

  @Override
  public String getId() {
    return getContactId();
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
