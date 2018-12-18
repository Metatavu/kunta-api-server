package fi.metatavu.kuntaapi.test.server.unit.images;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import fi.metatavu.kuntaapi.server.images.ImageReader;
import fi.metatavu.kuntaapi.server.images.ImageScaler;

@RunWith (CdiTestRunner.class)
public class ImageScalerTest {
  
  @Inject
  private ImageReader imageReader;
  
  @Inject
  private ImageScaler imageScaler;

  @Test
  public void testScaleToFitLandscape() throws IOException {
    assertNotNull(imageScaler);
    try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("test-image-1000-667.jpg")) {
      BufferedImage bufferedImage = imageReader.readBufferedImage(imageStream);
      assertNotNull(bufferedImage);
      
      assertEquals(1000, bufferedImage.getWidth());
      assertEquals(667, bufferedImage.getHeight());
      
      BufferedImage scaledImage = imageScaler.scaleToFit(bufferedImage, 400);
      
      assertEquals(400, scaledImage.getWidth());
      assertEquals(266, scaledImage.getHeight());
    }
  }

  @Test
  public void testScaleToFitPortrait() throws IOException {
    assertNotNull(imageScaler);
    try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("test-image-667-1000.jpg")) {
      BufferedImage bufferedImage = imageReader.readBufferedImage(imageStream);
      assertNotNull(bufferedImage);

      assertEquals(667, bufferedImage.getWidth());
      assertEquals(1000, bufferedImage.getHeight());
      
      BufferedImage scaledImage = imageScaler.scaleToFit(bufferedImage, 400);
      
      assertEquals(266, scaledImage.getWidth());
      assertEquals(400, scaledImage.getHeight());
    }
  }

  @Test
  public void testScaleToCoverLandscape() throws IOException {
    assertNotNull(imageScaler);
    try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("test-image-1000-667.jpg")) {
      BufferedImage bufferedImage = imageReader.readBufferedImage(imageStream);
      assertNotNull(bufferedImage);
      
      assertEquals(1000, bufferedImage.getWidth());
      assertEquals(667, bufferedImage.getHeight());
      
      BufferedImage scaledImage = imageScaler.scaleToCover(bufferedImage, 600, true);
      
      assertEquals(899, scaledImage.getWidth());
      assertEquals(600, scaledImage.getHeight());
    }
  }

  @Test
  public void testScaleToCoverPortrait() throws IOException {
    assertNotNull(imageScaler);
    try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("test-image-667-1000.jpg")) {
      BufferedImage bufferedImage = imageReader.readBufferedImage(imageStream);
      assertNotNull(bufferedImage);
      
      assertEquals(667, bufferedImage.getWidth());
      assertEquals(1000, bufferedImage.getHeight());
      
      BufferedImage scaledImage = imageScaler.scaleToCover(bufferedImage, 600, true);
      
      assertEquals(600, scaledImage.getWidth());
      assertEquals(899, scaledImage.getHeight());
    }
  }

  @Test
  public void testScaleToCoverLandscapeScaleUp() throws IOException {
    assertNotNull(imageScaler);
    try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("test-image-1000-667.jpg")) {
      BufferedImage bufferedImage = imageReader.readBufferedImage(imageStream);
      assertNotNull(bufferedImage);
      
      assertEquals(1000, bufferedImage.getWidth());
      assertEquals(667, bufferedImage.getHeight());
      
      BufferedImage scaledImage = imageScaler.scaleToCover(bufferedImage, 1200, true);
      
      assertEquals(1000, scaledImage.getWidth());
      assertEquals(667, scaledImage.getHeight());
      
      scaledImage = imageScaler.scaleToCover(bufferedImage, 900, true);
      
      assertEquals(1000, scaledImage.getWidth());
      assertEquals(667, scaledImage.getHeight());
      
      scaledImage = imageScaler.scaleToCover(bufferedImage, 1200, false);

      assertEquals(1799, scaledImage.getWidth());
      assertEquals(1200, scaledImage.getHeight());
    }
  }

  @Test
  public void testScaleToCoverPortraitScaleUp() throws IOException {
    assertNotNull(imageScaler);
    try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("test-image-667-1000.jpg")) {
      BufferedImage bufferedImage = imageReader.readBufferedImage(imageStream);
      assertNotNull(bufferedImage);
      
      assertEquals(667, bufferedImage.getWidth());
      assertEquals(1000, bufferedImage.getHeight());
      
      BufferedImage scaledImage = imageScaler.scaleToCover(bufferedImage, 1200, true);
      
      assertEquals(667, scaledImage.getWidth());
      assertEquals(1000, scaledImage.getHeight());
      
      scaledImage = imageScaler.scaleToCover(bufferedImage, 900, true);
      
      assertEquals(667, scaledImage.getWidth());
      assertEquals(1000, scaledImage.getHeight());
      
      scaledImage = imageScaler.scaleToCover(bufferedImage, 1200, false);

      assertEquals(1200, scaledImage.getWidth());
      assertEquals(1799, scaledImage.getHeight());
    }
  }
}
