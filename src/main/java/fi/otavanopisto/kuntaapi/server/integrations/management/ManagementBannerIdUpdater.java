package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Banner;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.OrganizationBannersTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementBannerIdUpdater extends IdUpdater {

  private static final int WARMUP_TIME = 1000 * 60;
  private static final int TIMER_INTERVAL = 1000 * 60 * 5;
  private static final int PER_PAGE = 100;
  private static final int MAX_PAGES = 10;
  
  @Inject
  private Logger logger;

  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdController idController;
  
  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 

  @Inject
  private Event<TaskRequest> taskRequest;

  @Inject
  private OrganizationBannersTaskQueue organizationBannersTaskQueue;
  
  @Resource
  private TimerService timerService;
  
  @Override
  public String getName() {
    return "management-banner-ids";
  }
  
  @Override
  public void startTimer() {
    startTimer(WARMUP_TIME);
  }
  
  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }
  
  @Timeout
  public void timeout(Timer timer) {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      OrganizationEntityUpdateTask task = organizationBannersTaskQueue.next();
      if (task != null) {
        updateManagementBanners(task.getOrganizationId());
      } else {
        organizationBannersTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
      }
    }

    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }
  
  private void updateManagementBanners(OrganizationId organizationId) {
    DefaultApi api = managementApi.getApi(organizationId);

    checkRemovedManagementBanners(api, organizationId);

    List<Banner> managementBanners = new ArrayList<>();
    
    int page = 1;
    do {
      List<Banner> pageBanners = listManagementBanners(api, organizationId, page);
      managementBanners.addAll(pageBanners);
      if (pageBanners.isEmpty() || pageBanners.size() < PER_PAGE) {
        break;
      } else {
        page++;
      }
    } while (page < MAX_PAGES);
    
    for (int i = 0, l = managementBanners.size(); i < l; i++) {
      Banner managementBanner = managementBanners.get(i);
      BannerId bannerId = new BannerId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementBanner.getId()));
      taskRequest.fire(new TaskRequest(false, new IdTask<BannerId>(Operation.UPDATE, bannerId, (long) i)));
    }
  }
  
  private List<Banner> listManagementBanners(DefaultApi api, OrganizationId organizationId, Integer page) {
    fi.metatavu.management.client.ApiResponse<List<Banner>> response = api.wpV2BannerGet(null, page, PER_PAGE, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s banners failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }
  
  private void checkRemovedManagementBanners(DefaultApi api, OrganizationId organizationId) {
    List<BannerId> bannerIds = identifierController.listOrganizationBannerIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (BannerId bannerId : bannerIds) {
      BannerId managementBannerId = idController.translateBannerId(bannerId, ManagementConsts.IDENTIFIER_NAME);
      if (managementBannerId != null) {
        ApiResponse<Banner> response = api.wpV2BannerIdGet(managementBannerId.getId(), null, null, null);
        int status = response.getStatus();
        // If status is 404 the banner has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the banner should not longer be available throught API
        if (status == 404 || status == 403) {
          taskRequest.fire(new TaskRequest(false, new IdTask<BannerId>(Operation.REMOVE, bannerId)));
        }
      }
    }
  }
}
