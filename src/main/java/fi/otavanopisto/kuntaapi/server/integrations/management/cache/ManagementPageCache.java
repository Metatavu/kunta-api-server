package fi.otavanopisto.kuntaapi.server.integrations.management.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractPageCache;

@ApplicationScoped
public class ManagementPageCache extends AbstractPageCache {

  private static final long serialVersionUID = 3226908290673289499L;

  @Override
  public String getName() {
    return "management-pages";
  }

}
