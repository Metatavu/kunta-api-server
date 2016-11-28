package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;

@ApplicationScoped
public class BannerImageCache extends AbstractEntityRelationCache<BannerId, AttachmentId, Attachment> {
 
  private static final long serialVersionUID = 7837093523439014051L;

  @Override
  public String getCacheName() {
    return "banner-images";
  }
  
}
