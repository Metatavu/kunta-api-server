package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.ContactProvider;
import fi.otavanopisto.kuntaapi.server.utils.ListUtils;
import fi.metatavu.kuntaapi.server.rest.model.Contact;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class ContactController {
  
  @Inject
  private EntityController entityController;
  
  @Inject
  private Instance<ContactProvider> contactProviders;
  
  public List<Contact> listContacts(OrganizationId organizationId, Integer firstResult, Integer maxResults) {
    List<Contact> result = new ArrayList<>();
   
    for (ContactProvider contactProvider : getContactProviders()) {
      result.addAll(contactProvider.listOrganizationContacts(organizationId));
    }
    
    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
  }

  public Contact findContact(OrganizationId organizationId, ContactId contactId) {
    for (ContactProvider contactProvider : getContactProviders()) {
      Contact contact = contactProvider.findOrganizationContact(organizationId, contactId);
      if (contact != null) {
        return contact;
      }
    }
    
    return null;
  }
  
  private List<ContactProvider> getContactProviders() {
    List<ContactProvider> result = new ArrayList<>();
    
    Iterator<ContactProvider> iterator = contactProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}
