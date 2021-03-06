package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.ejb3.annotation.Pool;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.AnnouncementId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.AnnouncementIdTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.resources.AnnouncementResourceContainer;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsJob;
import fi.metatavu.kuntaapi.server.tasks.jms.JmsQueueProperties;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Announcement;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
@MessageDriven (
  activationConfig = {
    @ActivationConfigProperty (propertyName = JmsQueueProperties.DESTINATION_LOOKUP, propertyValue = AnnouncementIdTaskQueue.JMS_QUEUE),
    @ActivationConfigProperty(propertyName = JmsQueueProperties.MESSAGE_SELECTOR, propertyValue = JmsQueueProperties.TASK_MESSAGE_SELECTOR)
  }
)
@Pool(JmsQueueProperties.LOW_CONCURRENCY_POOL)
public class ManagementAnnouncementEntityDiscoverJob extends AbstractJmsJob<IdTask<AnnouncementId>> {

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

  @Override
  public void execute(IdTask<AnnouncementId> task) {
    AnnouncementId announcementId = task.getId();
    
    if (task.getOperation() == Operation.UPDATE) {
      updateManagementAnnouncement(announcementId, task.getOrderIndex());
    } else if (task.getOperation() == Operation.REMOVE) {
      deleteAnnouncement(announcementId);
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
      logger.warning(() -> String.format("Find organization %s announcement %s failed on [%d] %s", organizationId.getId(), announcementId.toString(), response.getStatus(), response.getMessage()));
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
