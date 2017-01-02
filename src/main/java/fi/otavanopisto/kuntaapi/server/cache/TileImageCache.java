package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;

@ApplicationScoped
public class TileImageCache extends AbstractEntityRelationCache<TileId, AttachmentId, Attachment> {
 
  private static final long serialVersionUID = 6199135136945121667L;

  @Override
  public String getCacheName() {
    return "tiles-images";
  }
  
}
