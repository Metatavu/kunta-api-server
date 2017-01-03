package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;

@ApplicationScoped
public class PageImageCache extends AbstractEntityRelationCache<PageId, AttachmentId, Attachment> {
 
  private static final long serialVersionUID = 7155469364819589614L;

  @Override
  public String getCacheName() {
    return "page-images";
  }
  
}
