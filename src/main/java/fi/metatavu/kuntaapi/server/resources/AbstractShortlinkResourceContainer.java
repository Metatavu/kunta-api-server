package fi.metatavu.kuntaapi.server.resources;

import fi.metatavu.kuntaapi.server.rest.model.Shortlink;
import fi.metatavu.kuntaapi.server.id.ShortlinkId;

public abstract class AbstractShortlinkResourceContainer extends AbstractResourceContainer<ShortlinkId, Shortlink> {

  private static final long serialVersionUID = -1374010201093698629L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
