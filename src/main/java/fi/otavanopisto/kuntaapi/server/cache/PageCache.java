package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.rest.model.Page;

@ApplicationScoped
public class PageCache extends AbstractEntityCache<PageId, Page> {

  private static final long serialVersionUID = 6513524099128842893L;

  @Override
  public String getCacheName() {
    return "pages";
  }
  
}
