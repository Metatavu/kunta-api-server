package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvOrganizationListTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.lists.OrganizationListTaskQueue;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V8VmOpenApiOrganizationGuidPage;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvOrganizationIdUpdater extends AbstractPtvOrganizationIdDiscoverJob {
  
  @Inject
  private PtvApi ptvApi;

  @Inject
  private OrganizationListTaskQueue organizationListTaskQueue;
  
  @Override
  public String getName() {
    return "ptv-organization-ids";
  }
  
  @Override
  public boolean getIsPriority() {
    return false;
  }

  @Override
  public ApiResponse<V8VmOpenApiOrganizationGuidPage> getPage(Integer page) {
    return ptvApi.getOrganizationApi().apiV9OrganizationGet(null, null, page, PtvConsts.PUBLISHED_STATUS);
  }

  @Override
  public Long getOrderIndex(Integer page, int itemIndex, V8VmOpenApiOrganizationGuidPage guidPage) {
    return (long) (itemIndex + (page * guidPage.getPageSize()));
  }
  
  @Override
  public void timeout() {
    PtvOrganizationListTask task = organizationListTaskQueue.next();
    if (task != null) {
      discoverIds(task.getPage());
    } else if (organizationListTaskQueue.isEmptyAndLocalNodeResponsible()) {
      fillQueue();
    }
  }

  /**
   * Fills task queue with tasks for all pages
   */
  private void fillQueue() {
    Integer pageCount = getPage(1).getResponse().getPageCount();
    for (int page = 1; page < pageCount + 1; page++) {
      organizationListTaskQueue.enqueueTask(new PtvOrganizationListTask(false, page));
    }
  }
  
  @Override
  public void afterSuccess(V8VmOpenApiOrganizationGuidPage guidPage) {
    
  }

}
