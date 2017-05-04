package fi.otavanopisto.kuntaapi.server.discover;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.TimerService;
import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

public abstract class IdUpdater extends AbstractUpdater {

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;
  
  public abstract TimerService getTimerService();
  
  @Override
  public long getTimerWarmup() {
    if (systemSettingController.inTestMode()) {
      return 200l;
    }

    try {
      String key = String.format("id-updater.%s.warmup", getName());
      Long warmup = NumberUtils.createLong(System.getProperty(key));
      if (warmup != null) {
        return warmup;
      }
      
      logger.log(Level.WARNING, () -> String.format("Warmup for id updater %s is undefied", key));
    } catch (Exception e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to retrieve warmup", e);
      }
    }

    return 1000l * 60;
  }
  
  @Override
  public long getTimerInterval() {
    if (systemSettingController.inTestMode()) {
      return 1000l;
    }
    
    try {
      String key = String.format("id-updater.%s.interval", getName());
      Long interval = NumberUtils.createLong(System.getProperty(key));
      if (interval != null) {
        return interval;
      }
      
      logger.log(Level.WARNING, () -> String.format("Interval for id updater %s is undefied", key));
    } catch (Exception e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to retrieve timer interval", e);
      }
    }
    
    return 1000l * 10;
  }
  
  @Override
  public boolean isEligibleToRun() {
    if (systemSettingController.inFailsafeMode()) {
      return false;
    }
    
    return systemSettingController.isNotTestingOrTestRunning();
  }
  
}
