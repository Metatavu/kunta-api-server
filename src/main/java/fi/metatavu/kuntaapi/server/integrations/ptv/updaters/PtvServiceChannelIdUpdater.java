package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V3VmOpenApiGuidPage;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvServiceChannelListTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.lists.ServiceChannelListTaskQueue;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceChannelIdUpdater extends AbstractPtvServiceChannelIdDiscoverJob {
  
  @Inject
  private PtvApi ptvApi;

  @Inject
  private ServiceChannelListTaskQueue serviceChannelListTaskQueue;
  
  @Override
  public String getName() {
    return "ptv-service-channel-ids";
  }
  
  @Override
  public ApiResponse<V3VmOpenApiGuidPage> getPage(Integer page) {
    return ptvApi.getServiceChannelApi(null).apiV9ServiceChannelGet(null, null, page, PtvConsts.PUBLISHED_STATUS);
  }

  @Override
  public Long getOrderIndex(Integer page, int itemIndex, V3VmOpenApiGuidPage guidPage) {
    return (long) (itemIndex + (page * guidPage.getPageSize()));
  }

  @Override
  public void afterSuccess(V3VmOpenApiGuidPage guidPage) {
    
  }

  @Override
  public boolean getIsPriority() {
    return false;
  }

  @Override
  public void timeout() {
    PtvServiceChannelListTask task = serviceChannelListTaskQueue.next();
    if (task != null) {
      discoverIds(task.getPage());
    } else if (serviceChannelListTaskQueue.isEmptyAndLocalNodeResponsible()) {
      fillQueue();
    }
  }

  /**
   * Fills task queue with tasks for all pages
   */
  private void fillQueue() {
    Integer pageCount = getPage(1).getResponse().getPageCount();
    for (int page = 1; page < pageCount + 1; page++) {
      serviceChannelListTaskQueue.enqueueTask(new PtvServiceChannelListTask(false, page));
    }
  }

}
