package fi.metatavu.kuntaapi.server.locking;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Controller for cluster-wide locks
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ClusterLockController {
  
  @Inject
  private ClusterLock clusterLock;
  
  /**
   * Obtains a cluster-wide lock and schedules it's unlocking on the transaction end.
   * 
   * @param key lock key
   * @return true if lock has been created successfully, false if lock is already present
   */
  public boolean lockUntilTransactionCompletion(String key) {
    return clusterLock.lockUntilTransactionCompletion(key);
  }

}
