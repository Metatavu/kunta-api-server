package fi.otavanopisto.kuntaapi.server.cache;

import java.util.List;

import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.id.PageId;

public abstract class AbstractPageContentCache extends AbstractEntityCache<PageId, List<LocalizedValue>> {

  private static final long serialVersionUID = -1884653357761998317L;
  
}