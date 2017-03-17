package fi.otavanopisto.kuntaapi.server.integrations.casem.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPageResourceContainer;

@ApplicationScoped
public class CaseMPageCache extends AbstractPageResourceContainer {

  private static final long serialVersionUID = -5748105617264972537L;

  @Override
  public String getName() {
    return "casem-pages";
  }

}
