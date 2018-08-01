package fi.metatavu.kuntaapi.server.integrations;

import java.util.List;

import fi.metatavu.kuntaapi.server.id.ContactId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.rest.model.Contact;

/**
 * Interface that describes a single contact provider
 * 
 * @author Antti Leppä
 */
public interface ContactProvider {
  
  /**
   * Finds a single organization contact
   * 
   * @param organizationId organization id
   * @param contactId contact id
   * @return single organization contact or null if not found
   */
  public Contact findOrganizationContact(OrganizationId organizationId, ContactId contactId);

  /**
   * Lists contacts in an organization
   * 
   * @param organizationId organization id
   * @return organization contacts
   */
  public List<Contact> listOrganizationContacts(OrganizationId organizationId);
  
}