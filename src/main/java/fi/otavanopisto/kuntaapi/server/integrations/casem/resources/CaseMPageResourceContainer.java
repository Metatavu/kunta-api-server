package fi.otavanopisto.kuntaapi.server.integrations.casem.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPageResourceContainer;

@ApplicationScoped
public class CaseMPageResourceContainer extends AbstractPageResourceContainer {

  private static final long serialVersionUID = -5748105617264972537L;

  @Override
  public String getName() {
    return "casem-pages";
  }

}
