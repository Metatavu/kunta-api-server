package fi.otavanopisto.kuntaapi.server.integrations.mwp;

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
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementApi;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementImageLoader;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.mwp.client.ApiResponse;

/**
 * Abstract base class for mwp providers
 * 
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S3306")
public abstract class AbstractMwpProvider {
  
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
  
  protected AttachmentId getImageAttachmentId(Integer id) {
    AttachmentId mwpId = new AttachmentId(MwpConsts.IDENTIFIER_NAME, String.valueOf(id));
    AttachmentId kuntaApiId = idController.translateAttachmentId(mwpId, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId == null) {
      logger.info(String.format("Found new mwp attachment %d", id));
      Identifier newIdentifier = identifierController.createIdentifier(mwpId);
      kuntaApiId = new AttachmentId(KuntaApiConsts.IDENTIFIER_NAME, newIdentifier.getKuntaApiId());
    }
    
    return kuntaApiId;
  }
  
  protected Integer getMediaId(AttachmentId attachmentId) {
    AttachmentId mwpAttachmentId = idController.translateAttachmentId(attachmentId, MwpConsts.IDENTIFIER_NAME);
    if (mwpAttachmentId == null) {
      logger.info(String.format("Could not translate %s into mwp ", attachmentId.toString()));
      return null;
    }
    
    return NumberUtils.createInteger(mwpAttachmentId.getId());
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
  
  protected Attachment translateAttachment(fi.otavanopisto.mwp.client.model.Attachment featuredMedia) {
    Integer size = managementImageLoader.getImageSize(featuredMedia.getSourceUrl());
    AttachmentId id = getImageAttachmentId(featuredMedia.getId());
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
      localizedValue.setLanguage(MwpConsts.DEFAULT_LOCALE);
      localizedValue.setValue(value);
      result.add(localizedValue);
    }
    
    return result;
  }
  
  protected PageId translatePageId(Long pageId) {
    if (pageId == null) {
      return null;
    }
    
    return translatePageId(pageId.intValue());
  }
  
  protected PageId translatePageId(Integer pageId) {
    if (pageId == null) {
      return null;
    }
    
    PageId mwpId = new PageId(MwpConsts.IDENTIFIER_NAME, String.valueOf(pageId));
    return idController.translatePageId(mwpId, KuntaApiConsts.IDENTIFIER_NAME);
  }
}
