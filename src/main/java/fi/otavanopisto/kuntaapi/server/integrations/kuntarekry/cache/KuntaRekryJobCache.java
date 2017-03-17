package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.cache;

import fi.otavanopisto.kuntaapi.server.cache.AbstractJobCache;

public class KuntaRekryJobCache extends AbstractJobCache {

  private static final long serialVersionUID = -3909462228089482785L;

  @Override
  public String getName() {
    return "kunta-rekry-jobs";
  }

}
