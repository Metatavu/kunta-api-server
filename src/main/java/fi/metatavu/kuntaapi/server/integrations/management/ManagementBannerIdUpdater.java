package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.discover.IdUpdater;
import fi.metatavu.kuntaapi.server.id.BannerId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.BannerIdTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.OrganizationBannersTaskQueue;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Banner;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementBannerIdUpdater extends IdUpdater {

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
  private BannerIdTaskQueue bannerIdTaskQueue;

  @Inject
  private OrganizationBannersTaskQueue organizationBannersTaskQueue;

  @Override
  public String getName() {
    return "management-banner-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationBannersTaskQueue.next();
    if (task != null) {
      updateManagementBanners(task.getOrganizationId());
    } else if (organizationBannersTaskQueue.isEmptyAndLocalNodeResponsible()) {
      organizationBannersTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
  }
  
  private void updateManagementBanners(OrganizationId organizationId) {
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
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
      bannerIdTaskQueue.enqueueTask(new IdTask<BannerId>(false, Operation.UPDATE, bannerId, (long) i));
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
        ApiResponse<Object> response = api.wpV2BannerIdHead(managementBannerId.getId(), null, null, null);
        int status = response.getStatus();
        // If status is 404 the banner has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the banner should not longer be available throught API
        if (status == 404 || status == 403) {
          bannerIdTaskQueue.enqueueTask(new IdTask<BannerId>(false, Operation.REMOVE, bannerId));
        }
      }
    }
  }
}
