package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Announcement;
import fi.otavanopisto.kuntaapi.server.cache.AnnouncementCache;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.AttachmentIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementAnnouncementEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private AnnouncementCache announcementCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private AttachmentIdTaskQueue attachmentIdTaskQueue;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "management-announcements";
  }

  @PostConstruct
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Timeout
  public void timeout(Timer timer) {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      executeNextTask();
    }

    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
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

    Identifier identifier = identifierController.findIdentifierById(announcementId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(orderIndex, announcementId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, orderIndex);
    }

    identifierRelationController.setParentId(identifier, organizationId);
    
    AnnouncementId kuntaApiAnnouncementId = new AnnouncementId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Announcement announcement = managementTranslator.translateAnnouncement(kuntaApiAnnouncementId, managementAnnouncement);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(announcement));
    announcementCache.put(kuntaApiAnnouncementId, announcement);
  }

  private void deleteAnnouncement(AnnouncementId managementAnnouncementId) {
    OrganizationId organizationId = managementAnnouncementId.getOrganizationId();
    
    Identifier announcementIdentifier = identifierController.findIdentifierById(managementAnnouncementId);
    if (announcementIdentifier != null) {
      AnnouncementId kuntaApiAnnouncementId = new AnnouncementId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, announcementIdentifier.getKuntaApiId());
      modificationHashCache.clear(announcementIdentifier.getKuntaApiId());
      announcementCache.clear(kuntaApiAnnouncementId);
      identifierController.deleteIdentifier(announcementIdentifier);
    }
    
  }
}
