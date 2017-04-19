package fi.otavanopisto.kuntaapi.server.discover;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.AccessTimeout;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import java.util.Iterator;

@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
public abstract class EntityUpdater {
  
  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

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
  public abstract TimerService getTimerService();
  
  /**
   * Stops entity updater
   * 
   * @param cancelTimers if true all assiciated timers are also canceled
   */
  public void stop(boolean cancelTimers) {
    stopped = true;
    if (cancelTimers) {
      try {
        Collection<Timer> timers = getTimerService().getTimers();
        for (Timer timer : timers) {
          timer.cancel();
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Failed to cancel timer", e);
      }
    }
  }
  
  public int getTimerWarmup() {
    try {
      if (systemSettingController.inTestMode()) {
        return 200;
      }
      
      String key = String.format("entity-updater.%s.warmup", getName());
      Integer warmup = NumberUtils.createInteger(System.getProperty(key));
      if (warmup != null) {
        return warmup;
      }
      
      logger.log(Level.WARNING, () -> String.format("Warmup for entity updater %s is undefied", key));
    } catch (Exception e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to retrieve warmup", e);
      }
    }
    
    return 1000 * 60;
  }
  
  public int getTimerInterval() {
    try {
      if (systemSettingController.inTestMode()) {
        return 100;
      }
      
      String key = String.format("entity-updater.%s.interval", getName());
      Integer interval = NumberUtils.createInteger(System.getProperty(key));
      if (interval != null) {
        return interval;
      }

      logger.log(Level.WARNING, () -> String.format("Interval for entity updater %s is undefied", key));
    } catch (Exception e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to retrieve timer interval", e);
      }
    }
    
    return 1000 * 60;
  }

  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    getTimerService().createSingleActionTimer(duration, timerConfig);
  }
  
  @Timeout
  public void onTimeout() {
    if (!isStopped()) {
      try {
        if (isEligibleToRun()) {
          timeout();
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Timer throw an exception", e);
      } finally {
        try {
          Iterator<Timer> timers = getTimerService().getTimers().iterator();
          while(timers.hasNext()) {
            Timer timer = timers.next();
            timer.cancel();
          }
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Exception while canceling timer", e);
        } finally {
          startTimer(getTimerInterval());
        }
      }
    }
  }
  
  public abstract String getName();
  
  public boolean isStopped() {
    return stopped;
  }
  
  private boolean isEligibleToRun() {
    if (systemSettingController.inFailsafeMode()) {
      return false;
    }
    
    if (!systemSettingController.isNotTestingOrTestRunning()) {
      return false;
    }
    
    return true;
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
