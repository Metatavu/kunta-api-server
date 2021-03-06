package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.id.AttachmentId;

@ApplicationScoped
public abstract class AbstractAttachmentResourceContainer extends AbstractResourceContainer<AttachmentId, Attachment> {

  private static final long serialVersionUID = 8007965764944109964L;
  
  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
