package fi.metatavu.kuntaapi.server.tasks.metaflow;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang3.math.NumberUtils;

import fi.metatavu.kuntaapi.server.settings.SystemSettingController;

/**
 * Abstract base class for scheduled jobs
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public abstract class AbstractJob implements Job {

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  private Long lastRun;

  @PostConstruct
  public void postConstruct() {
    lastRun = null;
  }
  
  @Override
  public void run() {
    if (isEligibleToRun()) {
      runTx();
      lastRun = System.currentTimeMillis();
    }
  }
  
  @Transactional (value = TxType.REQUIRES_NEW)
  public void runTx() {
    timeout();
  }

  /**
   * Returns whether job is eligible to run.
   * 
   * @return whether job is eligible to run
   */
  protected boolean isEligibleToRun() {
    return true;
  }

  /**
   * Returns settings prefix
   * 
   * @return settings prefix
   */
  public abstract String getSettingPrefix();

  /**
   * Returns timer warmup in test mode
   * 
   * @return timer warmup in test mode
   */
  public abstract long getTestModeTimerWarmup();

  /**
   * Returns job delay in test mode
   * 
   * @return job delay in test mode
   */
  public abstract long getTestModeTimerInterval();
  
  /**
   * Returns time in milliseconds this task was last time run successfully
   * 
   * @return time in milliseconds this task was last time run successfully
   */
  public Long getLastRun() {
    return lastRun;
  }

  /**
   * Returns time in milliseconds from the last time this job was run successfully
   * 
   * @return time in milliseconds from the last time this job was run successfully
   */
  public Long getSinceLastRun() {
    if (lastRun == null) {
      return null;
    }
    
    return System.currentTimeMillis() - lastRun;
  }

  @Override
  public long getTimerWarmup() {
    try {
      if (systemSettingController.inTestMode()) {
        return getTestModeTimerWarmup();
      }
      
      String key = String.format("%s.%s.warmup", getSettingPrefix(), getName());
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
        return getTestModeTimerInterval();
      }
      
      String key = String.format("%s.%s.interval", getSettingPrefix(), getName());
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

}