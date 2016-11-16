package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.images.ImageReader;
import fi.otavanopisto.kuntaapi.server.images.ImageScaler;
import fi.otavanopisto.kuntaapi.server.images.ImageWriter;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.mwp.client.ApiResponse;

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
  private IdentifierController identifierController;

  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private ImageReader imageReader;

  @Inject
  private ImageWriter imageWriter;
  
  @Inject
  private ImageScaler imageScaler;
  
  protected AttachmentData scaleImage(AttachmentData imageData, Integer size) {
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
  
  protected AttachmentId getImageAttachmentId(OrganizationId organizationId, Integer id) {
    AttachmentId managementId = new AttachmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(id));
    AttachmentId kuntaApiId = idController.translateAttachmentId(managementId, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId == null) {
      logger.info(String.format("Found new management attachment %d", id));
      Identifier newIdentifier = identifierController.createIdentifier(managementId);
      kuntaApiId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, newIdentifier.getKuntaApiId());
    }
    
    return kuntaApiId;
  }
  
  protected Integer getMediaId(AttachmentId attachmentId) {
    AttachmentId managementAttachmentId = idController.translateAttachmentId(attachmentId, ManagementConsts.IDENTIFIER_NAME);
    if (managementAttachmentId == null) {
      logger.info(String.format("Could not translate %s into management id", attachmentId.toString()));
      return null;
    }
    
    return NumberUtils.createInteger(managementAttachmentId.getId());
  }

  protected fi.otavanopisto.mwp.client.model.Attachment findMedia(OrganizationId organizationId, Integer mediaId) {
    if (mediaId == null) {
      return null;
    }
    
    ApiResponse<fi.otavanopisto.mwp.client.model.Attachment> response = managementApi.getApi(organizationId).wpV2MediaIdGet(String.valueOf(mediaId), null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return response.getResponse();
    }
    
    return null;
  }
  
  protected Attachment translateAttachment(OrganizationId organizationId, fi.otavanopisto.mwp.client.model.Attachment featuredMedia) {
    Integer size = managementImageLoader.getImageSize(featuredMedia.getSourceUrl());
    AttachmentId id = getImageAttachmentId(organizationId, featuredMedia.getId());
    Attachment attachment = new Attachment();
    attachment.setContentType(featuredMedia.getMimeType());
    attachment.setId(id.getId());
    attachment.setSize(size != null ? size.longValue() : null);
    return attachment;
  }
  
  protected List<LocalizedValue> translateLocalized(String value) {
    // TODO: Support multiple locales 
    
    List<LocalizedValue> result = new ArrayList<>();
    
    if (StringUtils.isNotBlank(value)) {
      LocalizedValue localizedValue = new LocalizedValue();
      localizedValue.setLanguage(ManagementConsts.DEFAULT_LOCALE);
      localizedValue.setValue(value);
      result.add(localizedValue);
    }
    
    return result;
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
}
