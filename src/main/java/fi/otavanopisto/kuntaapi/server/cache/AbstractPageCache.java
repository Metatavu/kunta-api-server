package fi.otavanopisto.kuntaapi.server.cache;

import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.id.PageId;

public abstract class AbstractPageCache extends AbstractEntityCache<PageId, Page> {

  private static final long serialVersionUID = -4215756782113486241L;

}
