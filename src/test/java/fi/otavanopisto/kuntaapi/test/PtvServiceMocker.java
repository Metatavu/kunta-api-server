package fi.otavanopisto.kuntaapi.test;

import fi.metatavu.ptv.client.model.V6VmOpenApiService;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;

@SuppressWarnings ("squid:S1450")
public class PtvServiceMocker extends AbstractPtvMocker<V6VmOpenApiService> {

  private static final String BASE_PATH = String.format("/ptv/api/%s/Service", PtvConsts.VERSION);

  @Override
  public String getName() {
    return "services";
  }
  
  @Override
  public String getBasePath() {
    return BASE_PATH;
  }
  
  @Override
  public String getEntityId(V6VmOpenApiService entity) {
    return entity.getId().toString();
  }
  
}
