package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.discover.PageIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Page;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementPageIdUpdater extends IdUpdater {

  private static final int WARMUP_TIME = 1000 * 10;
  private static final int TIMER_INTERVAL = 5000;
  
  @Inject
  private Logger logger;

  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private Event<PageIdUpdateRequest> idUpdateRequest;

  private boolean stopped;
  private List<OrganizationId> queue;
  
  @Resource
  private TimerService timerService;
  
  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
  }

  @Override
  public String getName() {
    return "management-page-ids";
  }
  
  @Override
  public void startTimer() {
    stopped = false;
    startTimer(WARMUP_TIME);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }
  
  @Asynchronous
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getId();
      
      if (organizationSettingController.getSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL) == null) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(organizationId);
        queue.add(0, organizationId);
      } else {
        if (!queue.contains(organizationId)) {
          queue.add(organizationId);
        }
      }
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        updateManagementPages(queue.remove(0));
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateManagementPages(OrganizationId organizationId) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    List<Page> managementPages = listManagementPages(api, organizationId);
    for (Page managementPage : managementPages) {
      PageId pageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPage.getId()));
      idUpdateRequest.fire(new PageIdUpdateRequest(organizationId, pageId, false));
    }
  }
  
  private List<Page> listManagementPages(DefaultApi api, OrganizationId organizationId) {
    ApiResponse<List<Page>> response = api.wpV2PagesGet(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s pages failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }

}
