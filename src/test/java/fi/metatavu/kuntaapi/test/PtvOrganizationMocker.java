package fi.metatavu.kuntaapi.test;

import fi.metatavu.ptv.client.model.V8VmOpenApiOrganization;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;

@SuppressWarnings ("squid:S1075")
public class PtvOrganizationMocker extends AbstractPtvMocker<V8VmOpenApiOrganization> {
  
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
  public String getEntityId(V8VmOpenApiOrganization entity) {
    return entity.getId().toString();
  }
  
}
