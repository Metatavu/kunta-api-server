package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import fi.metatavu.management.client.model.Page;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.OrganizationPagesTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementPageIdUpdater extends IdUpdater {

  private static final int PER_PAGE = 50;
  private static final int MAX_PAGES = 100;
  
  @Inject
  private Logger logger;

  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private Event<TaskRequest> taskRequest;
  
  @Inject
  private OrganizationPagesTaskQueue organizationPagesTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "management-page-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationPagesTaskQueue.next();
    if (task != null) {
      updateManagementPages(task.getOrganizationId());
    } else {
      organizationPagesTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
  }
  
  @Override
  public TimerService getTimerService() {
    return timerService;
  }
    
  private void updateManagementPages(OrganizationId organizationId) {
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    List<Page> managementPages = new ArrayList<>();
    
    int page = 1;
    do {
      List<Page> pagePages = listManagementPages(api, organizationId, page);
      managementPages.addAll(pagePages);
      if (pagePages.isEmpty() || pagePages.size() < PER_PAGE) {
        break;
      } else {
        page++;
      }
    } while (page < MAX_PAGES);
    
    Collections.sort(managementPages, new PageComparator());
    
    for (int i = 0, l = managementPages.size(); i < l; i++) {
      Page managementPage = managementPages.get(i);
      PageId pageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPage.getId()));
      taskRequest.fire(new TaskRequest(false, new IdTask<PageId>(Operation.UPDATE, pageId, (long) i)));
    }
  }
  
  private List<Page> listManagementPages(DefaultApi api, OrganizationId organizationId, Integer page) {
    ApiResponse<List<Page>> response = api.wpV2PagesGet(null, page, PER_PAGE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s pages failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }
  
  private class PageComparator implements Comparator<Page> {

    @Override
    public int compare(Page page1, Page page2) {
      Integer order1 = page1.getMenuOrder();
      Integer order2 = page2.getMenuOrder();
      
      if (order1 == null) {
        order1 = 0;
      }
      
      if (order2 == null) {
        order2 = 0;
      }
      
      int result = order1.compareTo(order2);
      if (result == 0) {
        String title1 = getTitle(page1);
        String title2 = getTitle(page2);
        
        if (title1 == title2) {
          return 0;
        }
        
        if (title1 == null) {
          return -1;
        }
        
        if (title2 == null) {
          return 1;
        }
        
        return title1.compareToIgnoreCase(title2);
      }

      return result;
    }
    
    private String getTitle(Page page) {
      if (page.getTitle() == null) {
        return null;
      }
      
      return page.getTitle().getRendered();
    }

  }

}
