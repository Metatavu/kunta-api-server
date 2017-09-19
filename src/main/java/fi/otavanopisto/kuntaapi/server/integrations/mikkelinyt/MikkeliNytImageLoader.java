package fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.integrations.AbstractImageLoader;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

@ApplicationScoped
public class MikkeliNytImageLoader extends AbstractImageLoader {
  
  private static final String[] SIZES = {"1140", "1000"};

  @Inject
  private Logger logger;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  public AttachmentData getImageData(AttachmentId mikkeliNytId) {
    String imageBaseUrl = organizationSettingController.getSettingValue(mikkeliNytId.getOrganizationId(), MikkeliNytConsts.ORGANIZATION_SETTING_IMAGEBASEURL);
    if (StringUtils.isNotBlank(imageBaseUrl)) {
      for (String size : SIZES) {
        String imageUrl = String.format("%s/%s/%s", imageBaseUrl, size, mikkeliNytId.getId());
        AttachmentData imageData = getImageData(imageUrl);
        if (imageData != null) {
          return imageData;
        }
      }
    }
    
    logger.severe(() -> String.format("Image imageBaseUrl has not been configured properly for organization %s", mikkeliNytId.getOrganizationId()));
    
    return null;
  }
  
}
