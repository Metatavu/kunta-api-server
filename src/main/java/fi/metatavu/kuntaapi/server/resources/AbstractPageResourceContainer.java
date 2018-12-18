package fi.metatavu.kuntaapi.server.resources;

import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.metatavu.kuntaapi.server.id.PageId;

public abstract class AbstractPageResourceContainer extends AbstractResourceContainer<PageId, Page> {

  private static final long serialVersionUID = -4215756782113486241L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
