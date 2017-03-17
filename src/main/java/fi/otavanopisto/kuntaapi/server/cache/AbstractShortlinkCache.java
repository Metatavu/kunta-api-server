package fi.otavanopisto.kuntaapi.server.cache;

import fi.metatavu.kuntaapi.server.rest.model.Shortlink;
import fi.otavanopisto.kuntaapi.server.id.ShortlinkId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;

public abstract class AbstractShortlinkCache extends AbstractResourceContainer<ShortlinkId, Shortlink> {

  private static final long serialVersionUID = -1374010201093698629L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
