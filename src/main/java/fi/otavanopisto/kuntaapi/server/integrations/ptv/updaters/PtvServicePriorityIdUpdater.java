package fi.otavanopisto.kuntaapi.server.integrations.ptv.updaters;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V3VmOpenApiGuidPage;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvApi;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServicePriorityIdUpdater extends AbstractPtvServiceIdUpdater {
  
  @Inject
  private PtvApi ptvApi;
  
  @Resource
  private TimerService timerService;

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
  public TimerService getTimerService() {
    return timerService;
  }

  @Override
  public ApiResponse<V3VmOpenApiGuidPage> getPage() {
    currentUpdateStart = OffsetDateTime.now();
    return ptvApi.getServiceApi().apiV4ServiceGet(lastUpdate, null);
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
