package fi.otavanopisto.kuntaapi.server.cache;

import fi.metatavu.kuntaapi.server.rest.model.Shortlink;
import fi.otavanopisto.kuntaapi.server.id.ShortlinkId;

public abstract class AbstractShortlinkCache extends AbstractEntityCache<ShortlinkId, Shortlink> {

  private static final long serialVersionUID = -1374010201093698629L;

}
