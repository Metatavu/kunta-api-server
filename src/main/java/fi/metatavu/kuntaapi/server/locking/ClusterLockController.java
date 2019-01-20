package fi.metatavu.kuntaapi.server.locking;

import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Controller for cluster-wide locks
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ClusterLockController {
  
  private static final long MAX_LOCK_WAIT_TIME = TimeUnit.MINUTES.toMillis(1);
  
  @Inject
  private ClusterLock clusterLock;
  
  /**
   * Obtains a cluster-wide lock and schedules it's unlocking on the transaction end.
   * 
   * @param key lock key
   * @return true if lock has been created successfully, false if lock is already present
   */
  public boolean lockUntilTransactionCompletion(String key) {
    long timeout = System.currentTimeMillis() + MAX_LOCK_WAIT_TIME;
    
    while (!clusterLock.lockUntilTransactionCompletion(key) && (System.currentTimeMillis() < timeout)) {
      try {
        TimeUnit.MILLISECONDS.sleep(200);
      } catch (InterruptedException e) {
        return false;
      }
    }
    
    return true;
  }

}
