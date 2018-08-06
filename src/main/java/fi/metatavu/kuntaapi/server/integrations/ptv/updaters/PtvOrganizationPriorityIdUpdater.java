package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.VmOpenApiOrganizationGuidPage;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvOrganizationPriorityIdUpdater extends AbstractPtvOrganizationIdDiscoverJob {
  
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
    return "ptv-organization-priority-ids";
  }

  @Override
  public ApiResponse<VmOpenApiOrganizationGuidPage> getPage() {
    currentUpdateStart = OffsetDateTime.now();
    return ptvApi.getOrganizationApi().apiV7OrganizationGet(lastUpdate.minusMinutes(UPDATE_SLACK_MINUTE), null, false);
  }

  @Override
  public Long getOrderIndex(int itemIndex, VmOpenApiOrganizationGuidPage guidPage) {
    return null;
  }

  @Override
  public void afterSuccess(VmOpenApiOrganizationGuidPage guidPage) {
    lastUpdate = currentUpdateStart;
  }

}
