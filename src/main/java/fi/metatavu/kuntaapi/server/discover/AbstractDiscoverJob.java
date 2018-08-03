package fi.metatavu.kuntaapi.server.discover;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.AbstractJob;

public abstract class AbstractDiscoverJob extends AbstractJob {

  private static final int UPDATER_WARNING_THRESHOLD_SLACK = 1000 * 120;
  private static final int UPDATER_CRITICAL_THRESHOLD_SLACK = 1000 * 240;
  private static final int UPDATER_WARNING_THRESHOLD_MULTIPLIER = 20;
  private static final int UPDATER_CRITICAL_THRESHOLD_MULTIPLIER = 50;

  @Inject
  private SystemSettingController systemSettingController;

  @Override
  public boolean isEligibleToRun() {
    if (systemSettingController.inFailsafeMode()) {
      return false;
    }
    
    return systemSettingController.isNotTestingOrTestRunning();
  }
  
  /**
   * Returns updater health status
   * 
   * @return updater health status
   */
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

}
