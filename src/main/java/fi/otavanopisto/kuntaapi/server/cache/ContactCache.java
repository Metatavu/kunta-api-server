package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;
import fi.metatavu.kuntaapi.server.rest.model.Contact;

@ApplicationScoped
public class ContactCache extends AbstractResourceContainer<ContactId, Contact> {
  
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
