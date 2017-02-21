package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.otavanopisto.casem.client.model.Content;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.integrations.casem.tasks.MeetingDataUpdateTask;
import fi.otavanopisto.kuntaapi.server.integrations.casem.tasks.MeetingDataUpdateTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class CaseMMeetingEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;
  
  @Inject
  private Logger logger;
  
  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private CaseMCacheUpdater updater;
  
  @Inject
  private MeetingDataUpdateTaskQueue meetingDataUpdateTaskQueue;

  @Resource
  private TimerService timerService;
  
  @Override
  public String getName() {
    return "casem-meetings";
  }

  @PostConstruct
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Timeout
  public void timeout(Timer timer) {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      MeetingDataUpdateTask task = meetingDataUpdateTaskQueue.next();
      if (task != null) {
        try {
          ObjectMapper objectMapper = new ObjectMapper();
          Content meetingContent = objectMapper.readValue(task.getMeetingContent(), Content.class);
          List<Content> meetingItemContents  = objectMapper.readValue(task.getMeetingItemContents(), new TypeReference<List<Content>>() { });
          updater.updateMeeting(task.getMeetingPageId(), meetingContent, meetingItemContents);
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Failed to process casem meeting update request", e); 
        }
      }
    }
    
    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }

}
