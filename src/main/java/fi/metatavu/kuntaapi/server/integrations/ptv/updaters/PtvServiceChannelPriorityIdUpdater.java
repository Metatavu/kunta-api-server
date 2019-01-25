package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V3VmOpenApiGuidPage;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvServiceChannelListTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.lists.PriorityServiceChannelListTaskQueue;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceChannelPriorityIdUpdater extends AbstractPtvServiceChannelIdDiscoverJob {
  
  private static final int UPDATE_SLACK_MINUTE = 3;
  
  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private PriorityServiceChannelListTaskQueue priorityServiceChannelListTaskQueue;
  
  private OffsetDateTime currentUpdateStart;
  
  private OffsetDateTime lastUpdate;
    
  @PostConstruct
  public void init() {
    lastUpdate = OffsetDateTime.now().minusHours(1);
  }
  
  @Override
  public String getName() {
    return "ptv-service-channel-priority-ids";
  }

  @Override
  public String getEnabledSettingKey() {
    return PtvConsts.SYSTEM_SETTING_PRIORITY_SERVICE_CHANNEL_UPDATER_ENABLED;
  }
  
  @Override
  public ApiResponse<V3VmOpenApiGuidPage> getPage(Integer page) {
    currentUpdateStart = OffsetDateTime.now();
    return ptvApi.getServiceChannelApi(null).apiV9ServiceChannelGet(lastUpdate.minusMinutes(UPDATE_SLACK_MINUTE), null, null, PtvConsts.PUBLISHED_STATUS);
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
  public boolean getIsPriority() {
    return true;
  }
  
  @Override
  public void timeout() {
    PtvServiceChannelListTask task = priorityServiceChannelListTaskQueue.next();
    if (task != null) {
      discoverIds(task.getPage());
    } else if (priorityServiceChannelListTaskQueue.isEmptyAndLocalNodeResponsible()) {
      fillQueue();
    }
  }
  
  /**
   * Adds new priority list task into the queue
   */
  private void fillQueue() {
    priorityServiceChannelListTaskQueue.enqueueTask(new PtvServiceChannelListTask(true, 1));
  }
  
}
