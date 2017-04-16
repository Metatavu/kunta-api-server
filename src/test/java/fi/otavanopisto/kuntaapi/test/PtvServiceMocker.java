package fi.otavanopisto.kuntaapi.test;

import fi.metatavu.ptv.client.model.V4VmOpenApiService;

public class PtvServiceMocker extends AbstractPtvMocker<V4VmOpenApiService> {

  private static final String BASE_PATH = "/ptv/api/v4/Service";

  @Override
  public String getName() {
    return "services";
  }
  
  @Override
  public String getBasePath() {
    return BASE_PATH;
  }
  
  @Override
  public String getEntityId(V4VmOpenApiService entity) {
    return entity.getId();
  }
  
}
