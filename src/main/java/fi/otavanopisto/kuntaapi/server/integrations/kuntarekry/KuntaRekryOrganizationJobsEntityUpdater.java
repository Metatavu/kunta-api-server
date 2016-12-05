package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry;

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

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class KuntaRekryOrganizationJobsEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 100;

  @Inject
  private KuntaRekryClient kuntaRekryClient; 

  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private OrganizationSettingController organizationSettingController;

  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<OrganizationId> queue;

  @PostConstruct
  public void init() {
    queue = new ArrayList<>();
  }

  @Override
  public String getName() {
    return "organization-jobs";
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
      String apiUri = organizationSettingController.getSettingValue(event.getId(), KuntaRekryConsts.ORGANIZATION_SETTING_APIURI);
      if (StringUtils.isBlank(apiUri)) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(event.getId());
        queue.add(0, event.getId());
      } else {
        if (!queue.contains(event.getId())) {
          queue.add(event.getId());
        }
      }      
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        updateOrganizationJobs(queue.remove(0));          
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateOrganizationJobs(OrganizationId organizationId) {
    kuntaRekryClient.refreshJobs(organizationId);
    
    for (KuntaRekryJob kuntaRekryJob : kuntaRekryClient.listJobs(organizationId)) {
      JobId kuntaRekryId = new JobId(organizationId, KuntaRekryConsts.IDENTIFIER_NAME, String.valueOf(kuntaRekryJob.getJobId())); 
      Identifier identifier = identifierController.findIdentifierById(kuntaRekryId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(kuntaRekryId);
      }
      
      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaRekryJob));
    }
  }

}
