package fi.metatavu.kuntaapi.server.resources;

import java.util.List;

import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.id.PageId;

public abstract class AbstractPageContentCache extends AbstractResourceContainer<PageId, List<LocalizedValue>> {

  private static final long serialVersionUID = -1884653357761998317L;

  @Override
  public String getEntityType() {
    return "contents";
  }
   
}