package fi.metatavu.kuntaapi.server.integrations.management;

import java.awt.image.BufferedImage;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.images.ImageReader;
import fi.metatavu.kuntaapi.server.images.ImageScaler;
import fi.metatavu.kuntaapi.server.images.ImageWriter;
import fi.metatavu.kuntaapi.server.images.ScaledImageStore;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;

public abstract class AbstractAttachmentImageProvider {

  @Inject
  private ImageReader imageReader;

  @Inject
  private ImageWriter imageWriter;
  
  @Inject
  private ImageScaler imageScaler;

  @Inject
  private ScaledImageStore scaledImageStore;

  /**
   * Returns image data. Returned image is scaled if size parameter is given.
   * 
   * @param kuntaApiAttachmentId attachmentId
   * @param size size where the image should be scaled. Returns original if null is given
   * @return image data
   */
  protected AttachmentData getImageData(AttachmentId kuntaApiAttachmentId, Integer size) {
    if (size != null) {
      AttachmentData storedImage = scaledImageStore.getStoredImage(kuntaApiAttachmentId, size);
      if (storedImage != null) {
        return storedImage;
      }
    }
    
    AttachmentData attachmentData = getAttachmentData(kuntaApiAttachmentId);
    
    if (size != null) {
      AttachmentData storedImage = scaleImage(attachmentData, size);
      if (storedImage != null) {
        return scaledImageStore.storeScaledImage(kuntaApiAttachmentId, size, storedImage);
      }
    }
    
    return attachmentData;
  }

  /**
   * Returns image attachment data
   * 
   * @param kuntaApiAttachmentId attachmentId
   * @return image attachment data
   */
  protected abstract AttachmentData getAttachmentData(AttachmentId kuntaApiAttachmentId);

  /**
   * Scales image
   * 
   * @param imageData original image data
   * @param size desired size
   * @return scaled image data or null if scaling has failed
   */
  private AttachmentData scaleImage(AttachmentData imageData, Integer size) {
    if (imageData == null || imageData.getData() == null || imageScaler.isUnscaleableType(imageData.getType())) {
      return null;
    }
    
    BufferedImage bufferedImage = imageReader.readBufferedImage(imageData.getData());
    if (bufferedImage != null) {
      String formatName = imageWriter.getFormatName(imageData.getType());
      
      BufferedImage scaledImage = imageScaler.scaleToCover(bufferedImage, size, true);
      byte[] scaledImageData = imageWriter.writeBufferedImage(scaledImage, formatName);
      if (scaledImageData != null) {
        String contentType = imageWriter.getContentTypeForFormatName(formatName);
        return new AttachmentData(contentType, scaledImageData);
      }
    }
    
    return null;
  }
}
