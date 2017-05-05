package fi.otavanopisto.kuntaapi.test;

import fi.metatavu.ptv.client.model.V5VmOpenApiOrganization;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;

@SuppressWarnings ("squid:S1075")
public class PtvOrganizationMocker extends AbstractPtvMocker<V5VmOpenApiOrganization> {
  
  private static final String BASE_PATH = String.format("/ptv/api/%s/Organization", PtvConsts.VERSION);

  @Override
  public String getName() {
    return "organizations";
  }
  
  @Override
  public String getBasePath() {
    return BASE_PATH;
  }
  
  @Override
  public String getEntityId(V5VmOpenApiOrganization entity) {
    return entity.getId();
  }
  
}
