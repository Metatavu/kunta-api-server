package fi.otavanopisto.kuntaapi.server.integrations.ptv.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractOrganizationResourceContainer;

@ApplicationScoped
public class PtvOrganizationResourceContainer extends AbstractOrganizationResourceContainer {

  private static final long serialVersionUID = 9000403587525417755L;

  @Override
  public String getName() {
    return "ptv-organization";
  }

}
