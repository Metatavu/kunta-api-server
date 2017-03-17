package fi.otavanopisto.kuntaapi.server.integrations.management.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractShortlinkCache;

@ApplicationScoped
public class ManagementShortlinkCache extends AbstractShortlinkCache {

  private static final long serialVersionUID = -5315510394261705312L;

  @Override
  public String getName() {
    return "management-shortlinks";
  }

}
