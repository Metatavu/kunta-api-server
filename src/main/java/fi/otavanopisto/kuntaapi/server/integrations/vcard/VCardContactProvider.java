package fi.otavanopisto.kuntaapi.server.integrations.vcard;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Contact;
import fi.otavanopisto.kuntaapi.server.cache.ContactCache;
import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.ContactProvider;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class VCardContactProvider implements ContactProvider {
  
  @Inject
  private ContactCache contactCache;
  
  @Override
  public Contact findOrganizationContact(OrganizationId organizationId, ContactId contactId) {
    return contactCache.get(contactId);
  }

  @Override
  public List<Contact> listOrganizationContacts(OrganizationId organizationId) {
    List<ContactId> contactIds = contactCache.getOragnizationIds(organizationId);
    List<Contact> result = new ArrayList<>(contactIds.size());
    
    for (ContactId contactId : contactIds) {
      Contact contact = contactCache.get(contactId);
      if (contact != null) {
        result.add(contact);
      }
    }
    
    return result;
  }

}
