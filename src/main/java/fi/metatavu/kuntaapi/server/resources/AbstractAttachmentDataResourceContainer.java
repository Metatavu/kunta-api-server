package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.AttachmentId;

@ApplicationScoped
public abstract class AbstractAttachmentDataResourceContainer extends AbstractBinaryResourceContainer<AttachmentId> {
  
  private static final long serialVersionUID = -1270254557272204205L;

  @Override
  public String getEntityType() {
    return "data";
  }
  
}
