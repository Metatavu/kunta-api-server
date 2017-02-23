package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry;

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

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.cache.KuntaRekryJobCache;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryJobEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryJobTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class KuntaRekryJobEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000 * 10;
  
  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private KuntaRekryTranslator kuntaRekryTranslator;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private KuntaRekryJobTaskQueue kuntaRekryJobTaskQueue;

  @Inject
  private KuntaRekryJobCache kuntaRekryJobCache;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "organization-jobs";
  }

  @Override
  public void startTimer() {
    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Timeout
  public void timeout(Timer timer) {
    executeNextTask();
    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }
  
  private void executeNextTask() {
    KuntaRekryJobEntityTask task = kuntaRekryJobTaskQueue.next();
    if (task != null) {
      updateKuntaRekryJob(task); 
    }
  }

  private void updateKuntaRekryJob(KuntaRekryJobEntityTask task) {
    KuntaRekryJob kuntaRekryJob = task.getEntity();
    OrganizationId organizationId = task.getOrganizationId();
    Long orderIndex = task.getOrderIndex();
    
    JobId kuntaRekryId = new JobId(organizationId, KuntaRekryConsts.IDENTIFIER_NAME, String.valueOf(kuntaRekryJob.getJobId())); 
    Identifier identifier = identifierController.findIdentifierById(kuntaRekryId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(orderIndex, kuntaRekryId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, orderIndex);
    }
    
    identifierRelationController.setParentId(identifier, organizationId);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaRekryJob));
    
    JobId kuntaApiJobId = new JobId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    Job job = kuntaRekryTranslator.translateJob(kuntaApiJobId, kuntaRekryJob);
    
    kuntaRekryJobCache.put(kuntaApiJobId, job);
  }

}