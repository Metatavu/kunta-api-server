package fi.otavanopisto.kuntaapi.server.images;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Image scaler 
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class ImageScaler {
 
  @Inject
  private Logger logger;
  
 /**
   * Scales image to cover size x size
   * 
   * @param originalImage image
   * @param size desired size
   * @param downScaleOnly whether to return original if both propotions are lower than desired size
   * @return scaled image
   */
  public BufferedImage scaleToCover(BufferedImage originalImage, int size, boolean downScaleOnly) {
    return scaleToCover(originalImage, size, downScaleOnly, null);
  }
  
  /**
   * Scales image to cover size x size. Accepts imageObserver
   * 
   * @param originalImage image
   * @param size desired size
   * @param downScaleOnly whether to return original if both propotions are lower than desired size
   * @param imageObserver image observer
   * @return scaled image
   */
  public BufferedImage scaleToCover(BufferedImage originalImage, int size, boolean downScaleOnly, ImageObserver imageObserver) {
    int width = originalImage.getWidth();
    int height = originalImage.getHeight();
    
    if (downScaleOnly && (width < size || height < size)) {
      return originalImage;
    }
    
    if (width > height) {
      width = -1;
      height = size;
    } else {
      width = size;
      height = -1;
    }
    
    Image scaledInstance = originalImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
    
    if (imageObserver != null) {
      scaledInstance.getWidth(imageObserver);
      scaledInstance.getHeight(imageObserver);        
    } else {
      scaledInstance.getWidth(null);
      scaledInstance.getHeight(null);
    }
    
    return toBufferedImage(scaledInstance);
  }
  
  /**
   * Down scales image to fix size x size
   * 
   * @param originalImage original image
   * @param size max width / height of new image
   * @return scaled image
   */
  public BufferedImage scaleToFit(BufferedImage originalImage, int size) {
    return scaleToFit(originalImage, size, null);
  }
  
  /**
   * Down scales image to fix size x size. Accepts image observer
   * 
   * @param originalImage original image
   * @param size max width / height of new image
   * @param imageObserver image observer
   * @return scaled image
   */
  public BufferedImage scaleToFit(BufferedImage originalImage, int size, ImageObserver imageObserver) {
    int width = size;
    int height = size;
    
    if ((originalImage.getHeight() < size) && (originalImage.getWidth() < size)) {
      return originalImage;
    }

    if ((originalImage.getHeight() / size) > (originalImage.getWidth() / size))
      width = -1;
    else
      height = -1;

    Image scaledInstance = originalImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
    
    if (imageObserver != null) {
      scaledInstance.getWidth(imageObserver);
      scaledInstance.getHeight(imageObserver);        
    } else {
      scaledInstance.getWidth(null);
      scaledInstance.getHeight(null);
    }
    
    return toBufferedImage(scaledInstance);
  }

  private BufferedImage toBufferedImage(Image image) {
    if (image instanceof BufferedImage) {
      return (BufferedImage) image;
    } else {
      // ToolKitImages are not part of official JDK, so we need to use reflection to obtain the image
      try {
        Method getBufferedImageMethod = image.getClass().getMethod("getBufferedImage");
        return (BufferedImage) getBufferedImageMethod.invoke(image);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Failed to retrieve buffered image", e);
      }
    }
    
    return null;
  }
  
}
