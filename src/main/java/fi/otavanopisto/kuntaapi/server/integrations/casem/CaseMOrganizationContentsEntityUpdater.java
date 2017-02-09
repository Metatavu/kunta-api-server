package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class CaseMOrganizationContentsEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000 * 60 * 10;

  @Inject
  private CaseMCacheUpdater cacheUpdater;
  
  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private OrganizationSettingController organizationSettingController;

  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<OrganizationId> nodeQueue;
  private List<OrganizationId> boardQueue;
  private List<OrganizationId> meetingQueue;
  private Queue queue;
  
  @PostConstruct
  public void init() {
    queue = Queue.NODES;
    nodeQueue = new ArrayList<>();
    boardQueue = new ArrayList<>();
    meetingQueue = new ArrayList<>();
  }

  @Override
  public String getName() {
    return "organization-casem";
  }

  @Override
  public void startTimer() {
    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
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
  
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      String apiUri = organizationSettingController.getSettingValue(event.getId(), CaseMConsts.ORGANIZATION_SETTING_BASEURL);
      if (StringUtils.isBlank(apiUri)) {
        return;
      }
      
      if (event.isPriority()) {
        nodeQueue.remove(event.getId());
        nodeQueue.add(0, event.getId());
      } else {
        if (!nodeQueue.contains(event.getId())) {
          nodeQueue.add(event.getId());
        }
      }      
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning()) {
        updateNext();
      }
      
      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateNext() {
    switch (queue) {
      case NODES:
        if (!nodeQueue.isEmpty()) {
          boardQueue.add(updateOrganizationNodes(nodeQueue.remove(0)));
          queue = queue.next();
        }
      break;
      case BOARDS:
        if (!boardQueue.isEmpty()) {
          meetingQueue.add(updateOrganizationBoards(boardQueue.remove(0)));
          queue = queue.next();
        }
      break;
      case MEETINGS:
        if (!meetingQueue.isEmpty()) {
          updateOrganizationMeetings(meetingQueue.remove(0));          
          queue = queue.next();
        }
      break;
    }
  }

  private OrganizationId updateOrganizationNodes(OrganizationId organizationId) {
    cacheUpdater.updateNodes(organizationId);
    return organizationId;
  }
  
  private OrganizationId updateOrganizationBoards(OrganizationId organizationId) {
    cacheUpdater.updateBoards(organizationId);
    return organizationId;
  }
  
  private OrganizationId updateOrganizationMeetings(OrganizationId organizationId) {
    cacheUpdater.updateMeetings(organizationId);
    return organizationId;
  }

  private enum Queue {
    
    NODES,
    
    BOARDS,
    
    MEETINGS;
    
    public Queue next() {
      return values()[(ordinal() + 1) % values().length];
    }
    
  }
}
