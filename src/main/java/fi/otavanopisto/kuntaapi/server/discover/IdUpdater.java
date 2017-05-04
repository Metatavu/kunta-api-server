package fi.otavanopisto.kuntaapi.server.discover;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.AccessTimeout;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
public abstract class IdUpdater {

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Resource
  private ManagedScheduledExecutorService managedScheduledExecutorService;
  
  @Resource
  private EJBContext ejbContext;
  
  private boolean stopped;
  
  @PostConstruct
  public void postConstruct() {
    stopped = false;
    startTimer(getTimerWarmup(), getTimerInterval());
  }
  
  @PreDestroy
  public void preDestroy() {
    stopped = true;
  }
  
  public abstract void timeout();
  public abstract TimerService getTimerService();
  
  /**
   * Stops id updater
   * 
   * @param cancelTimers if true all assiciated timers are also canceled
   */
  public void stop(boolean cancelTimers) {
    stopped = true;
    if (cancelTimers) {
      managedScheduledExecutorService.shutdownNow();
    }
  }
  
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
  
  private void startTimer(long warmup, long delay) {
    managedScheduledExecutorService.scheduleWithFixedDelay(() -> {
      UserTransaction userTransaction = ejbContext.getUserTransaction();
      try {
        //userTransaction = lookup();
        userTransaction.begin();
        if (!isStopped() && isEligibleToRun()) {
          logger.log(Level.INFO, String.format("Running timer %s", getName()));
          timeout();
        }
        userTransaction.commit();
      } catch (Exception ex) {
        logger.log(Level.SEVERE, String.format("Timer with name %s throw an exception", getName()), ex);
        try {
          if(userTransaction != null) {
              userTransaction.rollback();
          }
        } catch (SystemException e1) {
          logger.log(Level.SEVERE, "Failed to rollback transaction", e1);
        }
      }
    }, warmup, delay, TimeUnit.MILLISECONDS);
  }
  
  private UserTransaction lookup() {
    try {
      InitialContext ic = new InitialContext();
      return (UserTransaction)ic.lookup("java:comp/UserTransaction");
    } catch (NamingException ex) {
      logger.log(Level.SEVERE, "Failed to lookup UserTransaction", ex);
    }
    
    return null;
  }
  
  public abstract String getName();
  
  public boolean isStopped() {
    return stopped;
  }
  
  private boolean isEligibleToRun() {
    if (systemSettingController.inFailsafeMode()) {
      return false;
    }
    
    return systemSettingController.isNotTestingOrTestRunning();
  }
  
}
