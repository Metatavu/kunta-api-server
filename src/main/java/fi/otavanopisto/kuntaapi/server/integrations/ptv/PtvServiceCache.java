package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import fi.otavanopisto.kuntaapi.server.cache.AbstractServiceCache;

public class PtvServiceCache extends AbstractServiceCache {

  private static final long serialVersionUID = 7520071744540725295L;

  @Override
  public String getCacheName() {
    return "ptv-services";
  }

}
