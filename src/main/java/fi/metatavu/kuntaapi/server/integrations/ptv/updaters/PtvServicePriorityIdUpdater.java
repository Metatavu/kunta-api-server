package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvServiceListTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.lists.PriorityServiceListTaskQueue;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V3VmOpenApiGuidPage;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServicePriorityIdUpdater extends AbstractPtvServiceIdDiscoverJob {
  
  private static final int UPDATE_SLACK_MINUTE = 3;
  
  @Inject
  private PtvApi ptvApi;  
  
  @Inject
  private PriorityServiceListTaskQueue priorityServiceListTaskQueue;

  private OffsetDateTime currentUpdateStart;
  
  private OffsetDateTime lastUpdate;
  
  @PostConstruct
  public void init() {
    lastUpdate = OffsetDateTime.now().minusHours(1);
  }
  
  @Override
  public boolean getIsPriority() {
    return true;
  }

  @Override
  public String getName() {
    return "ptv-service-priority-ids";
  }
  
  @Override
  public ApiResponse<V3VmOpenApiGuidPage> getPage(Integer page) {
    currentUpdateStart = OffsetDateTime.now();
    return ptvApi.getServiceApi(null).apiV9ServiceGet(lastUpdate.minusMinutes(UPDATE_SLACK_MINUTE), null, null, PtvConsts.PUBLISHED_STATUS);
  }

  @Override
  public Long getOrderIndex(Integer page, int itemIndex, V3VmOpenApiGuidPage guidPage) {
    return null;
  }

  @Override
  public void afterSuccess(V3VmOpenApiGuidPage guidPage) {
    lastUpdate = currentUpdateStart;
  }
  
  @Override
  public void timeout() {
    PtvServiceListTask task = priorityServiceListTaskQueue.next();
    if (task != null) {
      discoverIds(task.getPage());
    } else if (priorityServiceListTaskQueue.isEmptyAndLocalNodeResponsible()) {
      fillQueue();
    }
  }
  
  /**
   * Adds new priority list task into the queue
   */
  private void fillQueue() {
    priorityServiceListTaskQueue.enqueueTask(new PtvServiceListTask(true, 1));
  }
}
