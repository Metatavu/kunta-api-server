package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;

@ApplicationScoped
public abstract class AbstractAttachmentCache extends AbstractResourceContainer<AttachmentId, Attachment> {

  private static final long serialVersionUID = 8007965764944109964L;
  
  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
