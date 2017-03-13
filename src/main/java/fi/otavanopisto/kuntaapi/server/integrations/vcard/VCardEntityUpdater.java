package fi.otavanopisto.kuntaapi.server.integrations.vcard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.chain.ChainingTextParser;
import fi.metatavu.kuntaapi.server.rest.model.Contact;
import fi.otavanopisto.kuntaapi.server.cache.ContactCache;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.BinaryResponse;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.vcard.tasks.OrganizationVCardsTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class VCardEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;

  @Inject
  private BinaryHttpClient binaryHttpClient;
  
  @Inject
  private VCardTranslator vCardTranslator;
  
  @Inject
  private ContactCache contactCache;
  
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
  
  @Resource
  private TimerService timerService;
  
  @Override
  public String getName() {
    return "vcard-contacts";
  }

  @Override
  public void timeout() {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      OrganizationEntityUpdateTask task = organizationVCardsTaskQueue.next();
      if (task != null) {
        updateContacts(task.getOrganizationId());
      } else {
        organizationVCardsTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(VCardConsts.ORGANIZATION_SETTING_URL));
      }
    }
  }

  private void updateContacts(OrganizationId organizationId) {
    String url = getUrl(organizationId);
    if (StringUtils.isBlank(url)) {
      return;
    }
    
    String username = getUsername(organizationId);
    String password = getPassword(organizationId);
    
    Response<BinaryResponse> response = binaryHttpClient.downloadBinary(url, username, password);
    if (response.isOk()) {
      BinaryResponse responseEntity = response.getResponseEntity();
      try {
        updateContacts(organizationId, parseVCards(responseEntity));
      } catch (IOException e) {
        logger.log(Level.SEVERE, String.format("Failed to read VCard stream for organization %s", organizationId.toString()), e);
      }
    } else {
      logger.severe(String.format("Organization %s vcard contact list download failed on [%d] %s", organizationId.toString(), response.getStatus(), response.getMessage()));
    }
  }

  private void updateContacts(OrganizationId organizationId, List<VCard> vCards) {
    List<ContactId> removedIds = identifierController.listOrganizationContactIdsBySource(organizationId, VCardConsts.IDENTIFIER_NAME);
    
    for (int i = 0; i < vCards.size(); i++) {
      VCard vCard = vCards.get(i);
      Long orderIndex = (long) i;
      ContactId contactId = updateContact(organizationId, vCard, orderIndex);
      if (contactId != null) {
        removedIds.remove(contactId);
      }
    }
    
    for (ContactId removedId : removedIds) {
      deleteContact(organizationId, removedId);
    }
  }

  private List<VCard> parseVCards(BinaryResponse responseEntity) throws IOException {
    try (InputStream inputStream = new ByteArrayInputStream(responseEntity.getData())) {
      return parseVCards(inputStream);
    }
  }
  
  private List<VCard> parseVCards(InputStream inputStream) throws IOException {
    ChainingTextParser<ChainingTextParser<?>> parser = Ezvcard.parse(inputStream);
    if (parser != null) {
      return parser.all();
    }
    
    return Collections.emptyList();
  }
  

  private ContactId updateContact(OrganizationId organizationId, VCard vCard, Long orderIndex) {
    String vCardUid = vCard.getUid().getValue();
    if (StringUtils.isBlank(vCardUid)) {
      logger.severe(String.format("Skipped VCard without uid in organization %s", organizationId));
      return null;
    }
    
    ContactId contactId = new ContactId(organizationId, VCardConsts.IDENTIFIER_NAME, vCardUid);
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, contactId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    ContactId kuntaApiContactId = new ContactId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    Contact contact = vCardTranslator.translateVCard(kuntaApiContactId, vCard);
    
    modificationHashCache.put(kuntaApiContactId.getId(), createPojoHash(contact));
    contactCache.put(kuntaApiContactId, contact);
    
    return contactId;
  }
  
  private void deleteContact(OrganizationId organizationId, ContactId contactId) {
    Identifier contactIdentifier = identifierController.findIdentifierById(contactId);
    if (contactIdentifier != null) {
      ContactId kuntaApiContactId = new ContactId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, contactIdentifier.getKuntaApiId());
      modificationHashCache.clear(contactIdentifier.getKuntaApiId());
      contactCache.clear(kuntaApiContactId);
      identifierController.deleteIdentifier(contactIdentifier);
    }
  }

  private String getUrl(OrganizationId organizationId) {
    return organizationSettingController.getSettingValue(organizationId, VCardConsts.ORGANIZATION_SETTING_URL);
  }

  private String getUsername(OrganizationId organizationId) {
    return organizationSettingController.getSettingValue(organizationId, VCardConsts.ORGANIZATION_SETTING_USERNAME);
  }

  private String getPassword(OrganizationId organizationId) {
    return organizationSettingController.getSettingValue(organizationId, VCardConsts.ORGANIZATION_SETTING_PASSWORD);
  }
  
}
