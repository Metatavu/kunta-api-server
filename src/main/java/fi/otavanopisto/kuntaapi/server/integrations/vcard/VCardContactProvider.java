package fi.otavanopisto.kuntaapi.server.integrations.vcard;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Contact;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.ContactProvider;
import fi.otavanopisto.kuntaapi.server.resources.ContactResourceContainer;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class VCardContactProvider implements ContactProvider {
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private ContactResourceContainer contactCache;
  
  @Override
  public Contact findOrganizationContact(OrganizationId organizationId, ContactId contactId) {
    if (identifierRelationController.isChildOf(organizationId, contactId)) {
      Contact contact = contactCache.get(contactId);
      if (contact != null && Boolean.FALSE.equals(contact.getPrivateContact())) {
        return contact;
      }
    }
    
    return null;
  }

  @Override
  public List<Contact> listOrganizationContacts(OrganizationId organizationId) {
    List<ContactId> contactIds = identifierRelationController.listContactIdsBySourceAndParentId(VCardConsts.IDENTIFIER_NAME, organizationId);
    List<Contact> result = new ArrayList<>(contactIds.size());
    
    for (ContactId contactId : contactIds) {
      Contact contact = contactCache.get(contactId);
      if (contact != null && Boolean.FALSE.equals(contact.getPrivateContact())) {
        result.add(contact);
      }
    }
    
    return result;
  }

}
