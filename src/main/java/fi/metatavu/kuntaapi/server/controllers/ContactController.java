package fi.metatavu.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Contact;
import fi.metatavu.kuntaapi.server.id.ContactId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.index.SearchResult;
import fi.metatavu.kuntaapi.server.index.search.ContactSearcher;
import fi.metatavu.kuntaapi.server.integrations.ContactProvider;
import fi.metatavu.kuntaapi.server.integrations.ContactSortBy;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.SortDir;
import fi.metatavu.kuntaapi.server.utils.ListUtils;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class ContactController {

  @Inject
  private Logger logger;

  @Inject
  private EntityController entityController;

  @Inject
  private ContactSearcher contactSearcher;

  @Inject
  private IdController idController;

  @Inject
  private Instance<ContactProvider> contactProviders;
  
  public List<Contact> listContacts(OrganizationId organizationId, Long firstResult, Long maxResults) {
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
  
  public SearchResult<Contact> searchContacts(OrganizationId organizationId, String queryString, ContactSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.severe(() -> String.format("Failed to translate organization %s into Kunta API id", organizationId.toString()));
      return SearchResult.emptyResult();
    }
    
    SearchResult<ContactId> searchResult = contactSearcher.searchContacts(kuntaApiOrganizationId.getId(), queryString, sortBy, sortDir, firstResult, maxResults);
    if (searchResult != null) {
      List<Contact> contacts = new ArrayList<>(searchResult.getResult().size());
      
      for (ContactId contactId : searchResult.getResult()) {
        Contact contact = findContact(organizationId, contactId);
        if (contact != null) {
          contacts.add(contact);
        }
      }
      
      return new SearchResult<>(contacts, searchResult.getTotalHits());
    }
    
    return SearchResult.emptyResult();
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
