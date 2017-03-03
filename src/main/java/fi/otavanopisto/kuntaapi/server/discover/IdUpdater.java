package fi.otavanopisto.kuntaapi.server.discover;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@SuppressWarnings ("squid:S1610")
public abstract class IdUpdater {

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  @Resource
  private TimerService timerService;

  private boolean stopped;
  
  @PostConstruct
  public void postConstruct() {
    stopped = false;
    startTimer(getTimerWarmup());
  }
  
  @PreDestroy
  public void preDestroy() {
    stopped = true;
  }
  
  public abstract void timeout();
  
  public int getTimerWarmup() {
    String key = String.format("id-updater.%s.warmup", getName());
    Integer warmup = NumberUtils.createInteger(systemSettingController.getSettingValue(key));
    if (warmup == null) {
      logger.log(Level.WARNING, () -> String.format("Warmup for id updater %s is undefied", key));
      return 1000 * 60;
    }
    
    return warmup;
  }
  
  public int getTimerInterval() {
    String key = String.format("id-updater.%s.interval", getName());
    Integer interval = NumberUtils.createInteger(systemSettingController.getSettingValue(key));
    if (interval == null) {
      logger.log(Level.WARNING, () -> String.format("Interval for id updater %s is undefied", key));
      return 1000 * 60;
    }
    
    return interval;
  }

  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }
  
  @Timeout
  public void onTimeout() {
    if (!isStopped()) {
      try {
        if (systemSettingController.isNotTestingOrTestRunning()) {
          timeout();
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Timer throw an exception", e);
      } finally {
        startTimer(systemSettingController.inTestMode() ? 1000 : getTimerInterval());
      }
    }
  }
  
  public abstract String getName();
  
  public boolean isStopped() {
    return stopped;
  }
  
}
