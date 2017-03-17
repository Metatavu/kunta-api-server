package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Announcement;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.AttachmentIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.resources.AnnouncementResourceContainer;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class ManagementAnnouncementEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
   
  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private AnnouncementResourceContainer announcementResourceContainer;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private AttachmentIdTaskQueue attachmentIdTaskQueue;
  
  @Override
  public String getName() {
    return "management-announcements";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }

  private void executeNextTask() {
    IdTask<AnnouncementId> task = attachmentIdTaskQueue.next();
    if (task != null) {
      AnnouncementId announcementId = task.getId();
      
      if (task.getOperation() == Operation.UPDATE) {
        updateManagementAnnouncement(announcementId, task.getOrderIndex());
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteAnnouncement(announcementId);
      }
    }
  }
  
  private void updateManagementAnnouncement(AnnouncementId announcementId, Long orderIndex) {
    OrganizationId organizationId = announcementId.getOrganizationId();
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Announcement> response = api.wpV2AnnouncementIdGet(announcementId.getId(), null, null, null);
    if (response.isOk()) {
      updateManagementAnnouncement(organizationId, response.getResponse(), orderIndex);
    } else {
      logger.warning(String.format("Find organization %s announcement %s failed on [%d] %s", organizationId.getId(), announcementId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementAnnouncement(OrganizationId organizationId, Announcement managementAnnouncement, Long orderIndex) {
    AnnouncementId announcementId = new AnnouncementId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementAnnouncement.getId()));

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, announcementId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    AnnouncementId kuntaApiAnnouncementId = new AnnouncementId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Announcement announcement = managementTranslator.translateAnnouncement(kuntaApiAnnouncementId, managementAnnouncement);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(announcement));
    announcementResourceContainer.put(kuntaApiAnnouncementId, announcement);
  }

  private void deleteAnnouncement(AnnouncementId managementAnnouncementId) {
    OrganizationId organizationId = managementAnnouncementId.getOrganizationId();
    
    Identifier announcementIdentifier = identifierController.findIdentifierById(managementAnnouncementId);
    if (announcementIdentifier != null) {
      AnnouncementId kuntaApiAnnouncementId = new AnnouncementId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, announcementIdentifier.getKuntaApiId());
      modificationHashCache.clear(announcementIdentifier.getKuntaApiId());
      announcementResourceContainer.clear(kuntaApiAnnouncementId);
      identifierController.deleteIdentifier(announcementIdentifier);
    }
    
  }
}
