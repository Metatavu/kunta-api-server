package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.images.ImageReader;
import fi.otavanopisto.kuntaapi.server.images.ImageScaler;
import fi.otavanopisto.kuntaapi.server.images.ImageWriter;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.management.client.ApiResponse;

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
  
  @Inject
  private ManagementTranslator managementTranslator;
  
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
  
  protected Attachment translateAttachment(OrganizationId organizationId, fi.metatavu.management.client.model.Attachment featuredMedia) {
    return managementTranslator.translateAttachment(organizationId, featuredMedia);
  }
  
  protected List<LocalizedValue> translateLocalized(String value) {
    return managementTranslator.translateLocalized(value);
  }
  
  protected PageId translatePageId(OrganizationId organizationId, Long pageId) {
    if (pageId == null) {
      return null;
    }
    
    return translatePageId(organizationId, pageId.intValue());
  }
  
  protected PageId translatePageId(OrganizationId organizationId, Integer pageId) {
    if (pageId == null) {
      return null;
    }
    
    PageId managementId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(pageId));
    return idController.translatePageId(managementId, KuntaApiConsts.IDENTIFIER_NAME);
  }
  
  protected AttachmentId getImageAttachmentId(OrganizationId organizationId, Integer id) {
    AttachmentId managementId = new AttachmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(id));
    AttachmentId kuntaApiId = idController.translateAttachmentId(managementId, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId == null) {
      logger.info(String.format("Could not translate management attachment %s into Kunta API Id", managementId));
      return null;
    }
    
    return kuntaApiId;
  }
}
