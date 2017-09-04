package fi.otavanopisto.kuntaapi.server.discover;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

public abstract class EntityUpdater extends AbstractUpdater {

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  @Override
  public long getTimerWarmup() {
    try {
      if (systemSettingController.inTestMode()) {
        return 200;
      }
      
      String key = String.format("entity-updater.%s.warmup", getName());
      Long warmup = NumberUtils.createLong(systemSettingController.getSettingValue(key));
      if (warmup != null) {
        return warmup;
      }
      
      logger.log(Level.WARNING, () -> String.format("Warmup for entity updater %s is undefied", key));
    } catch (Exception e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to retrieve warmup", e);
      }
    }
    
    return 1000l * 60;
  }
  
  @Override
  public long getTimerInterval() {
    try {
      if (systemSettingController.inTestMode()) {
        return 20l;
      }
      
      String key = String.format("entity-updater.%s.interval", getName());
      Long interval = NumberUtils.createLong(systemSettingController.getSettingValue(key));
      if (interval != null) {
        return interval;
      }

      logger.log(Level.WARNING, () -> String.format("Interval for entity updater %s is undefied", key));
    } catch (Exception e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to retrieve timer interval", e);
      }
    }
    
    return 1000l * 5;
  }
  
  @Override
  public boolean isEligibleToRun() {
    if (systemSettingController.inFailsafeMode()) {
      return false;
    }
    
    return systemSettingController.isNotTestingOrTestRunning();
  }
  
  protected String createPojoHash(Object entity) {
    try {
      return DigestUtils.md5Hex(new ObjectMapper().writeValueAsBytes(entity));
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to create hash", e);
    }
    
    return null;
  }
  
}
