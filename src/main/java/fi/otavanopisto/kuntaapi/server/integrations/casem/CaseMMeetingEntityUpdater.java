package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.otavanopisto.casem.client.model.Content;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.integrations.casem.tasks.MeetingDataUpdateTask;
import fi.otavanopisto.kuntaapi.server.integrations.casem.tasks.MeetingDataUpdateTaskQueue;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class CaseMMeetingEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;
  
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

  @Override
  public void timeout() {
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

}
