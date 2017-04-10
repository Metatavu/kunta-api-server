package fi.otavanopisto.kuntaapi.server.images;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.apache.commons.codec.binary.StringUtils;

/**
 * Image writer
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class ImageWriter {
 
  @Inject
  private Logger logger;
  
  /**
   * Returns preferred ImageIO format for given content type
   * 
   * @param contentType content type
   * @return preferred ImageIO format for given content type
   */
  public String getFormatName(String contentType) {
    if (StringUtils.equals(contentType, "image/png")) {
      return "png";
    } 

    return "jpg";
  }
  
  /**
   * Returns appropriate content type for given ImageIO format
   * 
   * @param formatName format name
   * @return appropriate content type for given ImageIO format
   */
  public String getContentTypeForFormatName(String formatName) {
    if (StringUtils.equals(formatName, "png")) {
      return "image/png";
    } 

    return "image/jpeg";
  }
  
  /**
   * Writes buffered image as byte array
   * 
   * @param image image
   * @param formatName target format name
   * @return image data as byte array or null when writing has failed
   */
  @SuppressWarnings ("squid:S1168")
  public byte[] writeBufferedImage(BufferedImage image, String formatName) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    
    try {
      if (ImageIO.write(image, formatName, outputStream)) {
        outputStream.flush();
        outputStream.close();
        return outputStream.toByteArray();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to write buffered image", e);
    }
    
    return null;
  }
  
}
