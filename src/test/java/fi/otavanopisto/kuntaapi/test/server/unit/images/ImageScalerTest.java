package fi.otavanopisto.kuntaapi.test.server.unit.images;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import fi.otavanopisto.kuntaapi.server.images.ImageReader;
import fi.otavanopisto.kuntaapi.server.images.ImageScaler;

@RunWith (CdiTestRunner.class)
public class ImageScalerTest {
  
  
  @Inject
  private ImageReader imageReader;

  @Inject
  private ImageScaler imageScaler;

  @Test
  public void testScaleMaxSize() throws IOException {
    assertNotNull(imageScaler);
    try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("test-image-1000.jpg")) {
      BufferedImage bufferedImage = imageReader.readBufferedImage(imageStream);
      assertNotNull(bufferedImage);
      
      assertEquals(1000, bufferedImage.getWidth());
      assertEquals(667, bufferedImage.getHeight());
      
      BufferedImage scaledImage = imageScaler.scaleMaxSize(bufferedImage, 400);
      
      assertEquals(400, scaledImage.getWidth());
      assertEquals(266, scaledImage.getHeight());
    }

  }
  
}
