package fi.otavanopisto.kuntaapi.server.integrations.management.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractShortlinkResourceContainer;

@ApplicationScoped
public class ManagementShortlinkCache extends AbstractShortlinkResourceContainer {

  private static final long serialVersionUID = -5315510394261705312L;

  @Override
  public String getName() {
    return "management-shortlinks";
  }

}
