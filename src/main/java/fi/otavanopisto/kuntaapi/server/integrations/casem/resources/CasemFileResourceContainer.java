package fi.otavanopisto.kuntaapi.server.integrations.casem.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractFileResourceContainer;

@ApplicationScoped
public class CasemFileResourceContainer extends AbstractFileResourceContainer {

  private static final long serialVersionUID = 4993959656647694079L;

  @Override
  public String getName() {
    return "casem-files";
  }

}
