package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.casem.tasks.OrganizationBoardsTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.casem.tasks.OrganizationMeetingsTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.casem.tasks.OrganizationNodesTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class CaseMOrganizationContentsEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;

  @Inject
  private CaseMCacheUpdater cacheUpdater;
  
  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private OrganizationSettingController organizationSettingController;

  @Inject
  private OrganizationNodesTaskQueue organizationNodesTaskQueue;
  
  @Inject
  private OrganizationBoardsTaskQueue organizationBoardsTaskQueue;
  
  @Inject
  private OrganizationMeetingsTaskQueue organizationMeetingsTaskQueue;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  
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
    OrganizationEntityUpdateTask nodesTask = organizationNodesTaskQueue.next();
    if (nodesTask != null) {
      updateOrganizationNodes(nodesTask.getOrganizationId());
    } else {
      OrganizationEntityUpdateTask boardTask = organizationBoardsTaskQueue.next();
      if (boardTask != null) {
        updateOrganizationBoards(boardTask.getOrganizationId());
      } else {
        OrganizationEntityUpdateTask meetingTask = organizationMeetingsTaskQueue.next();
        if (meetingTask != null) {
          updateOrganizationMeetings(meetingTask.getOrganizationId());
        } else {
          List<OrganizationId> organizationIds = organizationSettingController.listOrganizationIdsWithSetting( CaseMConsts.ORGANIZATION_SETTING_BASEURL);
          organizationNodesTaskQueue.enqueueTasks(organizationIds);
          organizationBoardsTaskQueue.enqueueTasks(organizationIds);
          organizationMeetingsTaskQueue.enqueueTasks(organizationIds);
        }
      }
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

}
