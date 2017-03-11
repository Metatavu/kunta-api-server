package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Announcement;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.OrganizationAnnouncementsTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementAnnouncementIdUpdater extends IdUpdater {

  private static final int PER_PAGE = 100;
  private static final int MAX_PAGES = 10;
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdController idController;

  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 

  @Inject
  private OrganizationAnnouncementsTaskQueue organizationAnnouncementsTaskQueue;
  
  @Inject
  private Event<TaskRequest> taskRequest;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "management-announcement-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationAnnouncementsTaskQueue.next();
    if (task != null) {
      updateManagementAnnouncements(task.getOrganizationId());
    } else {
      organizationAnnouncementsTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
  }
  
  private void updateManagementAnnouncements(OrganizationId organizationId) {
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);

    checkRemovedManagementAnnouncements(api, organizationId);

    List<Announcement> managementAnnouncements = new ArrayList<>();
    
    int page = 1;
    do {
      List<Announcement> pageAnnouncements = listManagementAnnouncements(api, organizationId, page);
      managementAnnouncements.addAll(pageAnnouncements);
      if (pageAnnouncements.isEmpty() || pageAnnouncements.size() < PER_PAGE) {
        break;
      } else {
        page++;
      }
    } while (page < MAX_PAGES);
    
    for (int i = 0, l = managementAnnouncements.size(); i < l; i++) {
      Announcement managementAnnouncement = managementAnnouncements.get(i);
      AnnouncementId announcementId = new AnnouncementId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementAnnouncement.getId()));
      taskRequest.fire(new TaskRequest(false, new IdTask<AnnouncementId>(Operation.UPDATE, announcementId, (long) i)));
    }
  }
  
  private List<Announcement> listManagementAnnouncements(DefaultApi api, OrganizationId organizationId, Integer page) {
    fi.metatavu.management.client.ApiResponse<List<Announcement>> response = api.wpV2AnnouncementGet(null, page, PER_PAGE, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s announcements failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }
  
  private void checkRemovedManagementAnnouncements(DefaultApi api, OrganizationId organizationId) {
    List<AnnouncementId> announcementIds = identifierController.listOrganizationAnnouncementIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (AnnouncementId announcementId : announcementIds) {
      AnnouncementId managementAnnouncementId = idController.translateAnnouncementId(announcementId, ManagementConsts.IDENTIFIER_NAME);
      if (managementAnnouncementId != null) {
        ApiResponse<Announcement> response = api.wpV2AnnouncementIdGet(managementAnnouncementId.getId(), null, null, null);
        int status = response.getStatus();
        // If status is 404 the announcement has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the announcement should not longer be available throught API
        if (status == 404 || status == 403) {
          taskRequest.fire(new TaskRequest(false, new IdTask<AnnouncementId>(Operation.REMOVE, announcementId)));
        }
      }
    }
  }

}
