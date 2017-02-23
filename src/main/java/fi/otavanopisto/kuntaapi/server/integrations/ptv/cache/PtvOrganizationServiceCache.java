package fi.otavanopisto.kuntaapi.server.integrations.ptv.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractOrganizationServiceCache;

@ApplicationScoped
public class PtvOrganizationServiceCache extends AbstractOrganizationServiceCache {

  private static final long serialVersionUID = 6100260276077235996L;

  @Override
  public String getCacheName() {
    return "ptv-organization-services";
  }

}
