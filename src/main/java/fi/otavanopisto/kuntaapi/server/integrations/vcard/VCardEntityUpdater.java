package fi.otavanopisto.kuntaapi.server.integrations.vcard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.chain.ChainingTextParser;
import fi.metatavu.kuntaapi.server.rest.model.Contact;
import fi.metatavu.kuntaapi.server.rest.model.ContactPhone;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveContact;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexableContact;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.vcard.tasks.OrganizationVCardsTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.resources.ContactResourceContainer;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class VCardEntityUpdater extends EntityUpdater<OrganizationEntityUpdateTask> {

  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;

  @Inject
  private VCardIdFactory vCardIdFactory;

  @Inject
  private VCardTranslator vCardTranslator;
  
  @Inject
  private ContactResourceContainer contactCache;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private OrganizationVCardsTaskQueue organizationVCardsTaskQueue;

  @Inject
  private Event<IndexRequest> indexRequest;

  @Inject
  private Event<IndexRemoveRequest> indexRemoveRequest;

  @Override
  public String getName() {
    return "vcard-contacts";
  }
  
  @Override
  public void execute(OrganizationEntityUpdateTask task) {
    updateContacts(task.getOrganizationId());
  }

  @Override
  public void timeout() {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      OrganizationEntityUpdateTask task = organizationVCardsTaskQueue.next();
      if (task != null) {
        execute(task);
      } else {
        if (organizationVCardsTaskQueue.isEmptyAndLocalNodeResponsible()) {
          organizationVCardsTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(VCardConsts.ORGANIZATION_VCARD_FILE));
        }
      }
    }
  }
  
  private void updateContacts(OrganizationId kuntaApiOrganizationId) {
    if (!hasFileSetting(kuntaApiOrganizationId)) {
      return;
    }
    
    File vCardFile = getVCardFile(kuntaApiOrganizationId);
    if (!vCardFile.exists()) {
      logger.log(Level.WARNING, () -> String.format("Organization %s VCard file %s does not exist", kuntaApiOrganizationId, vCardFile.getAbsolutePath()));
      return;
    }
    
    try (FileInputStream fileInputStream = new FileInputStream(vCardFile)) {
      updateContacts(kuntaApiOrganizationId, parseVCards(fileInputStream));
    } catch (IOException e) {
      logger.log(Level.SEVERE, String.format("Failed to read VCard stream for organization %s", kuntaApiOrganizationId.toString()), e); 
    }
  }

  private void updateContacts(OrganizationId kuntaApiOrganizationId, List<VCard> vCards) {
    List<ContactId> existingVCardIds = identifierController.listOrganizationContactIdsBySource(kuntaApiOrganizationId, VCardConsts.IDENTIFIER_NAME);
    
    for (int i = 0; i < vCards.size(); i++) {
      VCard vCard = vCards.get(i);
      Long orderIndex = (long) i;
      ContactId vcardContactId = updateContact(kuntaApiOrganizationId, vCard, orderIndex);
      if (vcardContactId != null) {
        existingVCardIds.remove(vcardContactId);
      }
    }
    
    for (ContactId removedVCardId : existingVCardIds) {
      deleteContact(removedVCardId);
    }
  }

  private List<VCard> parseVCards(InputStream inputStream) throws IOException {
    ChainingTextParser<ChainingTextParser<?>> parser = Ezvcard.parse(inputStream);
    if (parser != null) {
      return parser.all();
    }
    
    return Collections.emptyList();
  }
  
  private ContactId updateContact(OrganizationId kuntaApiOrganizationId, VCard vCard, Long orderIndex) {
    String vCardUid = vCard.getUid().getValue();
    if (StringUtils.isBlank(vCardUid)) {
      logger.severe(() -> String.format("Skipped VCard without uid in organization %s", kuntaApiOrganizationId));
      return null;
    }
    
    ContactId vcardContactId = vCardIdFactory.createContactId(kuntaApiOrganizationId, vCardUid);
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, vcardContactId);
    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    ContactId kuntaApiContactId = kuntaApiIdFactory.createFromIdentifier(ContactId.class, identifier);
    Contact contact = vCardTranslator.translateVCard(kuntaApiContactId, vCard);
    
    modificationHashCache.put(kuntaApiContactId.getId(), createPojoHash(contact));
    contactCache.put(kuntaApiContactId, contact);
    
    createIndexableContact(kuntaApiOrganizationId, contact, orderIndex);
    
    indexRequest.fire(new IndexRequest(createIndexableContact(kuntaApiOrganizationId, contact, orderIndex)));

    return vcardContactId;
  }
  
  private void deleteContact(ContactId vcardContactId) {
    Identifier contactIdentifier = identifierController.findIdentifierById(vcardContactId);
    if (contactIdentifier != null) {
      ContactId kuntaApiContactId = kuntaApiIdFactory.createFromIdentifier(ContactId.class, contactIdentifier);
      modificationHashCache.clear(contactIdentifier.getKuntaApiId());
      contactCache.clear(kuntaApiContactId);
      identifierController.deleteIdentifier(contactIdentifier);
      IndexRemoveContact indexRemove = new IndexRemoveContact();
      indexRemove.setContactId(kuntaApiContactId.getId());
      indexRemoveRequest.fire(new IndexRemoveRequest(indexRemove));
    }
  }

  private boolean hasFileSetting(OrganizationId organizationId) {
    return StringUtils.isNotBlank(organizationSettingController.getSettingValue(organizationId, VCardConsts.ORGANIZATION_VCARD_FILE));
  }
  
  private File getVCardFile(OrganizationId organizationId) {
    return new File(organizationSettingController.getSettingValue(organizationId, VCardConsts.ORGANIZATION_VCARD_FILE));
  }
  
  private IndexableContact createIndexableContact(OrganizationId kuntaApiOrganizationId, Contact contact, Long orderIndex) {
    List<String> phoneNumbers = new ArrayList<>();
    List<ContactPhone> phones = contact.getPhones();
    if (phones != null) {
      for (ContactPhone phone : phones) {
        if (StringUtils.isNotBlank(phone.getType())) {
          phoneNumbers.add(phone.getNumber());
        }
      }
    }
     
    IndexableContact result = new IndexableContact();
    result.setAdditionalInformations(contact.getAdditionalInformations());
    result.setContactId(contact.getId());
    result.setDisplayName(contact.getDisplayName());
    result.setEmails(contact.getEmails());
    result.setFirstName(contact.getFirstName());
    result.setLastName(contact.getLastName());
    result.setOrderIndex(orderIndex);
    result.setOrganization(contact.getOrganization());
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setOrganizationUnits(contact.getOrganizationUnits());
    result.setPhoneNumbers(phoneNumbers);
    result.setTitle(contact.getTitle());
    result.setPrivateContact(contact.getPrivateContact());
    
    return result;
  }
}
