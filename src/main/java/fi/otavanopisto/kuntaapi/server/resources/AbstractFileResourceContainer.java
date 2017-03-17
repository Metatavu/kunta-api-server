package fi.otavanopisto.kuntaapi.server.resources;

import fi.metatavu.kuntaapi.server.rest.model.FileDef;
import fi.otavanopisto.kuntaapi.server.id.FileId;

public abstract class AbstractFileResourceContainer extends AbstractResourceContainer<FileId, FileDef> {

  private static final long serialVersionUID = -2414990078298246580L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
