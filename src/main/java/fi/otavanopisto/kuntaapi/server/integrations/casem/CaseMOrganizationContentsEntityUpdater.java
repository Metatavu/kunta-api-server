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
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class CaseMOrganizationContentsEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000 * 60;

  @Inject
  private CaseMCacheUpdater cacheUpdater;
  
  @Inject
  private OrganizationSettingController organizationSettingController;

  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<OrganizationId> nodesQueue;
  private List<OrganizationId> contentsQueue;
  private Queue queue;
  
  @PostConstruct
  public void init() {
    queue = Queue.NODES;
    nodesQueue = new ArrayList<>();
    contentsQueue = new ArrayList<>();
  }

  @Override
  public String getName() {
    return "organization-contents";
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
  
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      String apiUri = organizationSettingController.getSettingValue(event.getId(), CaseMConsts.ORGANIZATION_SETTING_BASEURL);
      if (StringUtils.isBlank(apiUri)) {
        return;
      }
      
      if (event.isPriority()) {
        nodesQueue.remove(event.getId());
        nodesQueue.add(0, event.getId());
      } else {
        if (!nodesQueue.contains(event.getId())) {
          nodesQueue.add(event.getId());
        }
      }      
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (queue == Queue.CONTENTS) {
        if (!contentsQueue.isEmpty()) {
          updateOrganizationContents(contentsQueue.iterator().next());          
        }
        
        queue = Queue.NODES;
      } else if (queue == Queue.NODES) {
        if (!nodesQueue.isEmpty()) {
          updateOrganizationNodes(nodesQueue.iterator().next());          
        }
        
        queue = Queue.CONTENTS;
      }
      
      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateOrganizationNodes(OrganizationId organizationId) {
    cacheUpdater.updateNodes(organizationId);
    if (!contentsQueue.contains(organizationId)) {
      contentsQueue.add(organizationId);
    }
  }
  
  private void updateOrganizationContents(OrganizationId organizationId) {
    cacheUpdater.updateContents(organizationId);
  }

  private enum Queue {
    
    NODES,
    
    CONTENTS
    
  }
}
