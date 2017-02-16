package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import fi.metatavu.management.client.ApiResponse;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.images.ImageReader;
import fi.otavanopisto.kuntaapi.server.images.ImageScaler;
import fi.otavanopisto.kuntaapi.server.images.ImageWriter;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;

/**
 * Abstract base class for management providers
 * 
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S3306")
public abstract class AbstractManagementProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;

  @Inject
  private IdController idController;
  
  @Inject
  private ImageReader imageReader;

  @Inject
  private ImageWriter imageWriter;
  
  @Inject
  private ImageScaler imageScaler;

  protected AttachmentData scaleImage(AttachmentData imageData, Integer size) {
    if (imageData == null || imageData.getData() == null) {
      return null;
    }
    
    BufferedImage bufferedImage = imageReader.readBufferedImage(imageData.getData());
    if (bufferedImage != null) {
      BufferedImage scaledImage = imageScaler.scaleMaxSize(bufferedImage, size);
      byte[] scaledImageData = imageWriter.writeBufferedImageAsPng(scaledImage);
      if (scaledImageData != null) {
        return new AttachmentData("image/png", scaledImageData);
      }
    }
    
    return null;
  }
  
  protected Integer getMediaId(AttachmentId attachmentId) {
    AttachmentId managementAttachmentId = idController.translateAttachmentId(attachmentId, ManagementConsts.IDENTIFIER_NAME);
    if (managementAttachmentId == null) {
      logger.info(String.format("Could not translate %s into management id", attachmentId.toString()));
      return null;
    }
    
    return NumberUtils.createInteger(managementAttachmentId.getId());
  }

  protected fi.metatavu.management.client.model.Attachment findMedia(OrganizationId organizationId, Integer mediaId) {
    if (mediaId == null) {
      return null;
    }
    
    ApiResponse<fi.metatavu.management.client.model.Attachment> response = managementApi.getApi(organizationId).wpV2MediaIdGet(String.valueOf(mediaId), null, null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return response.getResponse();
    }
    
    return null;
  }
  
}
