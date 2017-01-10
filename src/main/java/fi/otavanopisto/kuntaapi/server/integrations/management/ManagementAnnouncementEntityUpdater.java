package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Announcement;
import fi.otavanopisto.kuntaapi.server.cache.AnnouncementCache;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.AnnouncementIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.AnnouncementIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdateRequestQueue;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementAnnouncementEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;

  @Inject
  private Logger logger;
  
  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private AnnouncementCache announcementCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private IdUpdateRequestQueue<AnnouncementIdUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = new IdUpdateRequestQueue<>(ManagementConsts.IDENTIFIER_NAME);
  }

  @Override
  public String getName() {
    return "management-announcements";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    stopped = false;
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  @Asynchronous
  public void onAnnouncementIdUpdateRequest(@Observes AnnouncementIdUpdateRequest event) {
    if (!stopped) {
      AnnouncementId announcementId = event.getId();
      
      if (!StringUtils.equals(announcementId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      queue.add(event);
    }
  }
  
  @Asynchronous
  public void onAnnouncementIdRemoveRequest(@Observes AnnouncementIdRemoveRequest event) {
    if (!stopped) {
      AnnouncementId announcementId = event.getId();
      
      if (!StringUtils.equals(announcementId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      deleteAnnouncement(event, announcementId);
    }
  }
  
  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      AnnouncementIdUpdateRequest updateRequest = queue.next();
      if (updateRequest != null) {
        updateManagementAnnouncement(updateRequest.getOrganizationId(), updateRequest.getId(), updateRequest.getOrderIndex());
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateManagementAnnouncement(OrganizationId organizationId, AnnouncementId announcementId, Long orderIndex) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Announcement> response = api.wpV2AnnouncementIdGet(announcementId.getId(), null, null);
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
      identifier = identifierController.createIdentifier(organizationId, orderIndex, announcementId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, organizationId, orderIndex);
    }
    
    AnnouncementId kuntaApiAnnouncementId = new AnnouncementId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Announcement announcement = managementTranslator.translateAnnouncement(kuntaApiAnnouncementId, managementAnnouncement);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(announcement));
    announcementCache.put(kuntaApiAnnouncementId, announcement);
  }

  private void deleteAnnouncement(AnnouncementIdRemoveRequest event, AnnouncementId managementAnnouncementId) {
    OrganizationId organizationId = event.getOrganizationId();
    
    Identifier announcementIdentifier = identifierController.findIdentifierById(managementAnnouncementId);
    if (announcementIdentifier != null) {
      AnnouncementId kuntaApiAnnouncementId = new AnnouncementId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, announcementIdentifier.getKuntaApiId());
      queue.remove(managementAnnouncementId);
      modificationHashCache.clear(announcementIdentifier.getKuntaApiId());
      announcementCache.clear(kuntaApiAnnouncementId);
      identifierController.deleteIdentifier(announcementIdentifier);
    }
    
  }
}
