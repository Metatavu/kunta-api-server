package fi.otavanopisto.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.metatavu.kuntaapi.server.rest.model.Contact;

@ApplicationScoped
public class ContactResourceContainer extends AbstractResourceContainer<ContactId, Contact> {
  
  private static final long serialVersionUID = 2614703866060990521L;

  @Override
  public String getName() {
    return "contacts";
  }
  
  @Override
  public String getEntityType() {
    return "resource";
  }

}
