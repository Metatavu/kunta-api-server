package fi.otavanopisto.kuntaapi.server.cache;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;

@ApplicationScoped
public class PageContentCache extends AbstractEntityCache<PageId, List<LocalizedValue>> {

  private static final long serialVersionUID = 6513524099128842893L;

  @Override
  public String getCacheName() {
    return "page-contents";
  }
  
}