package fi.otavanopisto.kuntaapi.server.images;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;

/**
 * Image store for scaled images
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ScaledImageStore {
 
  @Inject
  private Logger logger;
  
  /**
   * Returns scaled image data
   * 
   * @param kuntaApiAttachmentId attachmentId
   * @param size size
   * @return scaled image data or null if not found
   */
  public AttachmentData getStoredImage(AttachmentId kuntaApiAttachmentId, Integer size) {
    File imageFile = getImageFile(kuntaApiAttachmentId, size);
    if (imageFile.exists()) {
      ObjectMapper objectMapper = getObjectMapper();
      try {
        return objectMapper.readValue(imageFile, AttachmentData.class);
      } catch (IOException e) {
        logger.log(Level.WARNING, String.format("Failed to read stored scaled image %s", imageFile.getName()), e);
        if (!imageFile.delete()) {
          logger.log(Level.WARNING, String.format("Failed to delete stored scaled image %s", imageFile.getName()), e);  
        }
      }
    }
    
    return null; 
  }
  
  /**
   * Stores scaled image
   * 
   * @param kuntaApiAttachmentId attachmentId
   * @param size size
   * @param imageData image data
   * @return image data
   */
  public AttachmentData storeScaledImage(AttachmentId kuntaApiAttachmentId, Integer size, AttachmentData imageData) {
    File imageFile = getImageFile(kuntaApiAttachmentId, size);
    
    ObjectMapper objectMapper = getObjectMapper();
    try {
      objectMapper.writeValue(imageFile, imageData);
    } catch (IOException e) {
      logger.log(Level.WARNING, String.format("Failed to read stored scaled image %s", imageFile.getName()), e);
      if (imageFile.exists() && !imageFile.delete()) {
        logger.log(Level.WARNING, String.format("Failed to delete stored scaled image %s", imageFile.getName()), e);  
      }
    }
    
    return imageData;
  }
  
  /**
   * Lists all stored image sizes
   * 
   * @param kuntaApiAttachmentId attachmentId
   * @return all stored image sizes
   */
  public List<Integer> listStoredImageSizes(AttachmentId kuntaApiAttachmentId) {
    File[] storedFiles = listStoredImages(kuntaApiAttachmentId);

    List<Integer> result = new ArrayList<>(storedFiles.length);
    
    for (File storedFile : storedFiles) {
      result.add(Integer.parseInt(StringUtils.substringBeforeLast(storedFile.getName(), "-")));
    }
    
    return result;
  }
  
  /**
   * Removes all stored image files
   * 
   * @param kuntaApiAttachmentId attachmentId
   */
  public void purgeStoredImages(AttachmentId kuntaApiAttachmentId) {
    File[] storedImages = listStoredImages(kuntaApiAttachmentId);
    for (File storedImage : storedImages) {
      if (!storedImage.delete()) {
        logger.log(Level.WARNING, () -> String.format("Failed to delete stored scaled image %s", storedImage.getName()));  
      }
    }
  }

  private File[] listStoredImages(AttachmentId kuntaApiAttachmentId) {
    String fileNamePrefix = getFileNamePrefix(kuntaApiAttachmentId);
    return getTempDirectory().listFiles((File dir, String name) -> StringUtils.startsWith(name, fileNamePrefix));
  }
  
  private ObjectMapper getObjectMapper() {
    return new ObjectMapper(new SmileFactory());
  }
  
  private File getImageFile(AttachmentId kuntaApiAttachmentId, Integer size) {
    File tempDirectory = getTempDirectory();
    return new File(tempDirectory, getFileName(kuntaApiAttachmentId, size));
  }
  
  private File getTempDirectory() {
    return new File(System.getProperty("java.io.tmpdir"));
  }
  
  private String getFileName(AttachmentId kuntaApiAttachmentId, Integer size) {
    return String.format("%s-%d", getFileNamePrefix(kuntaApiAttachmentId), size);
  }
  
  private String getFileNamePrefix(AttachmentId kuntaApiAttachmentId) {
    return String.format("a-%s", kuntaApiAttachmentId.getId()); 
  }
  
}
