package fi.otavanopisto.kuntaapi.server.cache;

import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;

public abstract class AbstractPageCache extends AbstractResourceContainer<PageId, Page> {

  private static final long serialVersionUID = -4215756782113486241L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
