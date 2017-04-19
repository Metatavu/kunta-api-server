package fi.otavanopisto.kuntaapi.server.discover;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@Singleton
@ApplicationScoped
public class SchedulerIntervalUpdater {

  private static final int UPDATE_INTERVAL = 1000 * 60 * 5;
  
  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Resource
  private TimerService timerService;

  @PostConstruct
  public void postConstruct() {
    updateSchedulerIntervals();
  }
  
  @Timeout
  public void onTimeout() {
    updateSchedulerIntervals();
  }
  
  private void updateSchedulerIntervals() {
    try {
      assignSystemProperties("id-updater");
      assignSystemProperties("entity-updater");
    } finally {
      try {
        Iterator<Timer> timers = timerService.getTimers().iterator();
        while(timers.hasNext()) {
          Timer timer = timers.next();
          timer.cancel();
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Exception while canceling timer", e);
      } finally {
        startTimer();
      }
    }
  }
  
  private void assignSystemProperties(String prefix) {
    Map<String, String> settings = systemSettingController.getSettingsWithPrefix(prefix);
    for (Entry<String, String> setting : settings.entrySet()) {
      System.setProperty(setting.getKey(), setting.getValue());
    }
  }

  private void startTimer() {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(systemSettingController.inTestMode() ? 1000 : UPDATE_INTERVAL, timerConfig);
  }
  
}
