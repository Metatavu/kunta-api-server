package fi.otavanopisto.kuntaapi.server.integrations.management.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPageContentCache;

@ApplicationScoped
public class ManagementPageContentCache extends AbstractPageContentCache {

  private static final long serialVersionUID = 4103968741744421851L;

  @Override
  public String getName() {
    return "management-page-contents";
  }

}
