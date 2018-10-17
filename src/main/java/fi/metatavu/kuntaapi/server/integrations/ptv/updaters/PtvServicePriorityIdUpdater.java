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

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServicePriorityIdUpdater extends AbstractPtvServiceIdDiscoverJob {
  
  private static final int UPDATE_SLACK_MINUTE = 3;
  
  @Inject
  private PtvApi ptvApi;

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
  public ApiResponse<V3VmOpenApiGuidPage> getPage() {
    currentUpdateStart = OffsetDateTime.now();
    return ptvApi.getServiceApi(null).apiV8ServiceGet(null, lastUpdate.minusMinutes(UPDATE_SLACK_MINUTE), null, PtvConsts.PUBLISHED_STATUS);
  }

  @Override
  public Long getOrderIndex(int itemIndex, V3VmOpenApiGuidPage guidPage) {
    return null;
  }

  @Override
  public void afterSuccess(V3VmOpenApiGuidPage guidPage) {
    lastUpdate = currentUpdateStart;
  }

}
