package fi.metatavu.kuntaapi.server.locking;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.infinispan.Cache;

/**
 * Cluster lock bean. Used to create cluster-wide locks.
 * 
 * Bean should be used via ClusterLockController, not directly
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@Singleton
@Lock (LockType.WRITE)
public class ClusterLock {
  
  private final static int LOCK_EXPIRE = 1000 * 60;
  
  @Inject
  private Logger logger;

  @Inject
  private Event<UnlockEvent> unlockEvent; 

  @Resource(lookup = "java:jboss/infinispan/cache/kunta-api/clusterlock")
  private Cache<String, Long> lockContainer; 
  
  /**
   * Obtains a cluster-wide lock and schedules it's unlocking on the transaction end.
   * 
   * @param key lock key
   * @return true if lock has been created successfully, false if lock is already present
   */
  public boolean lockUntilTransactionCompletion(String key) {
    boolean locked = lock(key);
    
    if (locked) {
      unlockEvent.fire(new UnlockEvent(key));
    }
    
    return locked;
  }

  /**
   * Returns whether lock is present
   * 
   * @param key lock key
   * @return whether lock is present
   */
  public boolean isLocked(String key) {
    Long expires = lockContainer.get(key);
    if (expires == null) {
      return false;
    }
    
    if (System.currentTimeMillis() > expires) {
      logger.info(String.format("lock %s expired", key));
      return false;
    }
    
    return true;
  }

  /**
   * Obtains a cluster-wide lock
   * 
   * @param key lock key
   * @return true if lock has been created successfully, false if lock is already present
   */
  public boolean lock(String key) {
    if (isLocked(key)) {
      return false;
    }
    
    lockContainer.put(key, System.currentTimeMillis() + LOCK_EXPIRE);
    
    return true;
  }

  /**
   * Releases a cluster-wide lock
   * 
   * @param key lock key
   */
  public void unlock(String key) {
    lockContainer.remove(key);
  }
  
  /**
   * Event handler for releasing locks after transaction end
   * 
   * @param event event
   */
  public void onUnlockEvent(@Observes (during = TransactionPhase.AFTER_COMPLETION) UnlockEvent event) {
    unlock(event.getKey());
    
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest(String.format("unlocked %s after transaction", event.getKey()));
    }
  }
  
}
