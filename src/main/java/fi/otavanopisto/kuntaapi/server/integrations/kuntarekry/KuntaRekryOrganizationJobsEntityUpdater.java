package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry;

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
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks.OrganizationJobsTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class KuntaRekryOrganizationJobsEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000 * 60;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private KuntaRekryClient kuntaRekryClient; 

  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  @Inject
  private OrganizationJobsTaskQueue organizationJobsTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "organization-jobs";
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
      OrganizationEntityUpdateTask task = organizationJobsTaskQueue.next();
      if (task != null) {
        updateOrganizationJobs(task.getOrganizationId());
      } else {
        organizationJobsTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(KuntaRekryConsts.ORGANIZATION_SETTING_APIURI));
      }
    }

    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }

  private void updateOrganizationJobs(OrganizationId organizationId) {
    kuntaRekryClient.refreshJobs(organizationId);
    List<KuntaRekryJob> kuntaRekryJobs = kuntaRekryClient.listJobs(organizationId);
    
    for (int i = 0; i < kuntaRekryJobs.size(); i++) {
      KuntaRekryJob kuntaRekryJob = kuntaRekryJobs.get(i);
      Long orderIndex = (long) i;
      
      JobId kuntaRekryId = new JobId(organizationId, KuntaRekryConsts.IDENTIFIER_NAME, String.valueOf(kuntaRekryJob.getJobId())); 
      Identifier identifier = identifierController.findIdentifierById(kuntaRekryId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(orderIndex, kuntaRekryId);
      } else {
        identifier = identifierController.updateIdentifier(identifier, orderIndex);
      }
      
      identifierRelationController.setParentId(identifier, organizationId);
      
      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaRekryJob));
    }
  }

}
