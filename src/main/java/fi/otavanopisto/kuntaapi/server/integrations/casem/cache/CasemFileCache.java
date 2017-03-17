package fi.otavanopisto.kuntaapi.server.integrations.casem.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractFileResourceContainer;

@ApplicationScoped
public class CasemFileCache extends AbstractFileResourceContainer {

  private static final long serialVersionUID = 4993959656647694079L;

  @Override
  public String getName() {
    return "casem-files";
  }

}
