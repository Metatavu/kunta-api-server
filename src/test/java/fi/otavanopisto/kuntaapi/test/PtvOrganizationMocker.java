package fi.otavanopisto.kuntaapi.test;

import fi.metatavu.ptv.client.model.V4VmOpenApiOrganization;

public class PtvOrganizationMocker extends AbstractPtvMocker<V4VmOpenApiOrganization> {
  
  private static final String BASE_PATH = "/ptv/api/v4/Organization";

  @Override
  public String getName() {
    return "organizations";
  }
  
  @Override
  public String getBasePath() {
    return BASE_PATH;
  }
  
  @Override
  public String getEntityId(V4VmOpenApiOrganization entity) {
    return entity.getId();
  }
  
}
