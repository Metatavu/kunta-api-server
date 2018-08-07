package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V3VmOpenApiGuidPage;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceChannelIdUpdater extends AbstractPtvServiceChannelIdDiscoverJob {
  
  @Inject
  private PtvApi ptvApi;
  
  private Integer page;
  
  @PostConstruct
  public void init() {
    page = 1;
  }
  
  @Override
  public String getName() {
    return "ptv-service-channel-ids";
  }
  
  @Override
  public ApiResponse<V3VmOpenApiGuidPage> getPage() {
    return ptvApi.getServiceChannelApi(null).apiV8ServiceChannelGet(page, null, null, "published");
  }

  @Override
  public Long getOrderIndex(int itemIndex, V3VmOpenApiGuidPage guidPage) {
    return (long) (itemIndex + (page * guidPage.getPageSize()));
  }

  @Override
  public void afterSuccess(V3VmOpenApiGuidPage guidPage) {
    if ((page + 1) < guidPage.getPageCount()) {
      page++;
    } else {
      page = 1;
    }
  }

  @Override
  public boolean getIsPriority() {
    return false;
  }

}
