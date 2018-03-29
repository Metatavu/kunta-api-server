package fi.otavanopisto.kuntaapi.server.discover;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

public abstract class AbstractUpdater {

  private static final int UPDATER_WARNING_THRESHOLD_SLACK = 1000 * 120;
  private static final int UPDATER_CRITICAL_THRESHOLD_SLACK = 1000 * 240;
  private static final int UPDATER_WARNING_THRESHOLD_MULTIPLIER = 20;
  private static final int UPDATER_CRITICAL_THRESHOLD_MULTIPLIER = 50;

  @Inject
  private Logger logger;

  @Resource
  private ManagedScheduledExecutorService managedScheduledExecutorService;
  
  @Resource
  private EJBContext ejbContext;
  
  private Long lastRun;
  private boolean stopped;
  
  @PostConstruct
  public void postConstruct() {
    stopped = false;
    lastRun = null;
    startTimer(getTimerWarmup(), getTimerInterval());
  }
  
  @PreDestroy
  public void preDestroy() {
    stopped = true;
  }
  
  public abstract void timeout();
  public abstract long getTimerWarmup();
  public abstract long getTimerInterval();  
  public abstract String getName();
  public abstract boolean isEligibleToRun();
  
  public Long getLastRun() {
    return lastRun;
  }

  public Long getSinceLastRun() {
    if (lastRun == null) {
      return null;
    }
    
    return System.currentTimeMillis() - lastRun;
  }
  
  public UpdaterHealth getHealth() {
    if (getLastRun() == null) {
      return UpdaterHealth.UNKNOWN;
    }
    
    long sinceLastRun = getSinceLastRun();
    if (sinceLastRun > ((getTimerInterval() * UPDATER_CRITICAL_THRESHOLD_MULTIPLIER) + UPDATER_CRITICAL_THRESHOLD_SLACK)) {
      return UpdaterHealth.CRITICAL;
    }
    
    if (sinceLastRun > ((getTimerInterval() * UPDATER_WARNING_THRESHOLD_MULTIPLIER) + UPDATER_WARNING_THRESHOLD_SLACK)) {
      return UpdaterHealth.WARNING;
    }
    
    return UpdaterHealth.OK;
  }

  public boolean isStopped() {
    return stopped;
  }
  
  private void startTimer(long warmup, long delay) {
    managedScheduledExecutorService.scheduleWithFixedDelay(() -> {
      UserTransaction userTransaction = ejbContext.getUserTransaction();
      try {
        userTransaction.begin();
        
        if (!isStopped() && isEligibleToRun()) {
          logger.log(Level.FINE, String.format("Running timer %s", getName()));
          timeout();
        }
        
        userTransaction.commit();
        lastRun = System.currentTimeMillis();
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
  
}
