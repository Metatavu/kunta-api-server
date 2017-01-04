package fi.otavanopisto.kuntaapi.server.cache;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.PageId;

public abstract class AbstractPageImageCache extends AbstractEntityRelationCache<PageId, AttachmentId, Attachment> {

  private static final long serialVersionUID = -1110720806371780008L;
  
}
