package fi.otavanopisto.kuntaapi.server.integrations.casem.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPageContentCache;

@ApplicationScoped
public class CaseMPageContentResourceContainer extends AbstractPageContentCache {

  private static final long serialVersionUID = 5074106733901932323L;

  @Override
  public String getName() {
    return "casem-page-contents";
  }

}
