package fi.otavanopisto.kuntaapi.server.integrations.ptv.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractOrganizationServiceResourceContainer;

@ApplicationScoped
public class PtvOrganizationServiceCache extends AbstractOrganizationServiceResourceContainer {

  private static final long serialVersionUID = 6100260276077235996L;

  @Override
  public String getName() {
    return "ptv-organization-services";
  }

}
