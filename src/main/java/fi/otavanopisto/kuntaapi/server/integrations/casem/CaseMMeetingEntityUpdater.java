package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class CaseMMeetingEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;
  
  @Inject
  private CaseMCacheUpdater updater;

  @Resource
  private TimerService timerService;

  @Inject
  private Logger logger;
  
  private boolean stopped;
  
  private List<CaseMMeetingData> queue;

  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
  }

  @Override
  public String getName() {
    return "casem-meetings";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    stopped = false;
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  @Asynchronous
  public void onCaseMMeetingDataUpdateRequest(@Observes CaseMMeetingDataUpdateRequest event) {
    if (!stopped) {
      logger.info(String.format(" < Scheduled update for %s", event.getMeetingData().getMeetingPageId().toString()));
      queue.add(event.getMeetingData());
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        updater.updateMeeting(queue.remove(0));
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

}
