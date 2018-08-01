package fi.metatavu.kuntaapi.server.integrations.linkedevents;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.linkedevents.client.ApiResponse;
import fi.metatavu.linkedevents.client.ImageApi;
import fi.metatavu.linkedevents.client.model.Image;
import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.integrations.AbstractImageLoader;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.linkedevents.client.LinkedEventsApi;

@ApplicationScoped
public class LinkedEventsImageLoader extends AbstractImageLoader {
  
  @Inject
  private Logger logger;
  
  @Inject
  private LinkedEventsApi linkedEventsApi;

  public AttachmentData getImageData(AttachmentId linkedEventsAttachmentId) {
    ImageApi imageApi = linkedEventsApi.getImageApi(linkedEventsAttachmentId.getOrganizationId());
    ApiResponse<Image> response = imageApi.imageRetrieve(linkedEventsAttachmentId.getId());
    if (response.isOk()) {
      Image image = response.getResponse();
      return getImageData(image.getUrl());
    } else {
      logger.warning(String.format("Find event image %s failed on [%d] %s", linkedEventsAttachmentId, response.getStatus(), response.getMessage()));
    }
    
    return null;
  }
  
}
