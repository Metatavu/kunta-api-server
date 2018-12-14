package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvServiceListTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.lists.ServiceListTaskQueue;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V3VmOpenApiGuidPage;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceIdUpdater extends AbstractPtvServiceIdDiscoverJob {
  
  @Inject
  private PtvApi ptvApi;

  @Inject
  private ServiceListTaskQueue serviceListTaskQueue;
  
  @Override
  public String getName() {
    return "ptv-service-ids";
  }
  
  @Override
  public ApiResponse<V3VmOpenApiGuidPage> getPage(Integer page) {
    return ptvApi.getServiceApi(null).apiV8ServiceGet(page, null, null, PtvConsts.PUBLISHED_STATUS);
  }

  @Override
  public Long getOrderIndex(Integer page, int itemIndex, V3VmOpenApiGuidPage guidPage) {
    return (long) (itemIndex + (page * guidPage.getPageSize()));
  }
  
  @Override
  public void timeout() {
    PtvServiceListTask task = serviceListTaskQueue.next();
    if (task != null) {
      discoverIds(task.getPage());
    } else if (serviceListTaskQueue.isEmptyAndLocalNodeResponsible()) {
      fillQueue();
    }
  }

  /**
   * Fills service list task queue with tasks for all service list pages
   */
  private void fillQueue() {
    Integer pageCount = getPage(1).getResponse().getPageCount();
    for (int page = 1; page < pageCount + 1; page++) {
      serviceListTaskQueue.enqueueTask(new PtvServiceListTask(false, page));
    }
  }

  @Override
  public void afterSuccess(V3VmOpenApiGuidPage guidPage) {
  }

  @Override
  public boolean getIsPriority() {
    return false;
  }

}
