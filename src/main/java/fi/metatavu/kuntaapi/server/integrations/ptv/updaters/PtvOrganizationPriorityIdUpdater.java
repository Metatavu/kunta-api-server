package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V8VmOpenApiOrganizationGuidPage;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvOrganizationListTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.lists.PriorityOrganizationListTaskQueue;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvOrganizationPriorityIdUpdater extends AbstractPtvOrganizationIdDiscoverJob {
  
  private static final int UPDATE_SLACK_MINUTE = 3;

  @Inject
  private PtvApi ptvApi;

  @Inject
  private PriorityOrganizationListTaskQueue priorityOrganizationListTaskQueue;
  
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
    return "ptv-organization-priority-ids";
  }

  @Override
  public ApiResponse<V8VmOpenApiOrganizationGuidPage> getPage(Integer page) {
    currentUpdateStart = OffsetDateTime.now();
    return ptvApi.getOrganizationApi().apiV9OrganizationGet(lastUpdate.minusMinutes(UPDATE_SLACK_MINUTE), null, null, PtvConsts.PUBLISHED_STATUS);
  }

  @Override
  public Long getOrderIndex(Integer page, int itemIndex, V8VmOpenApiOrganizationGuidPage guidPage) {
    return null;
  }

  @Override
  public void afterSuccess(V8VmOpenApiOrganizationGuidPage guidPage) {
    lastUpdate = currentUpdateStart;
  }
  
  @Override
  public void timeout() {
    PtvOrganizationListTask task = priorityOrganizationListTaskQueue.next();
    if (task != null) {
      discoverIds(task.getPage());
    } else if (priorityOrganizationListTaskQueue.isEmptyAndLocalNodeResponsible()) {
      fillQueue();
    }
  }
  
  /**
   * Adds new priority list task into the queue
   */
  private void fillQueue() {
    priorityOrganizationListTaskQueue.enqueueTask(new PtvOrganizationListTask(true, 1));
  }
  
}
