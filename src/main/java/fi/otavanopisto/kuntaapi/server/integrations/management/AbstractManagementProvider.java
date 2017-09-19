package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import ezvcard.util.IOUtils;
import fi.metatavu.management.client.ApiResponse;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.management.resources.ManagementAttachmentDataResourceContainer;
import fi.otavanopisto.kuntaapi.server.resources.StoredBinaryData;

/**
 * Abstract base class for management providers
 * 
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S3306")
public abstract class AbstractManagementProvider extends AbstractAttachmentImageProvider {
  
  @Inject
  private Logger logger;

  @Inject
  private ManagementAttachmentDataResourceContainer managementAttachmentDataResourceContainer;
  
  @Inject
  private ManagementApi managementApi;

  @Inject
  private IdController idController;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Override
  protected AttachmentData getAttachmentData(AttachmentId kuntaApiAttachmentId) {
    AttachmentData storedAttachmentData = getStoredAttachmentData(kuntaApiAttachmentId);
    if (storedAttachmentData != null) {
      return storedAttachmentData;
    }
    
    Integer mediaId = getMediaId(kuntaApiAttachmentId);
    if (mediaId == null) {
      return null;
    }
    
    fi.metatavu.management.client.model.Attachment featuredMedia = findMedia(kuntaApiAttachmentId.getOrganizationId(), mediaId);
    if (featuredMedia == null) {
      return null;
    }
   
    return managementImageLoader.getImageData(featuredMedia.getSourceUrl());
  }
  
  private AttachmentData getStoredAttachmentData(AttachmentId attachmentId) {
    StoredBinaryData storedBinaryData = managementAttachmentDataResourceContainer.get(attachmentId);
    if (storedBinaryData != null) {
      try {
        return new AttachmentData(storedBinaryData.getContentType(), IOUtils.toByteArray(storedBinaryData.getDataStream()));
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Failed to read stream data", e);
      } 
    }
     
    return null;
  }
  
  private Integer getMediaId(AttachmentId attachmentId) {
    AttachmentId managementAttachmentId = idController.translateAttachmentId(attachmentId, ManagementConsts.IDENTIFIER_NAME);
    if (managementAttachmentId == null) {
      logger.info(() -> String.format("Could not translate %s into management id", attachmentId.toString()));
      return null;
    }

    return NumberUtils.createInteger(StringUtils.substringBefore(managementAttachmentId.getId(), "-"));
  }

  private fi.metatavu.management.client.model.Attachment findMedia(OrganizationId organizationId, Integer mediaId) {
    if (mediaId == null) {
      return null;
    }
    
    ApiResponse<fi.metatavu.management.client.model.Attachment> response = managementApi.getApi(organizationId).wpV2MediaIdGet(String.valueOf(mediaId), null, null);
    if (!response.isOk()) {
      logger.severe(() -> String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return response.getResponse();
    }
    
    return null;
  }
  
}
