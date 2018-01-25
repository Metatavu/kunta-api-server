package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry;

import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
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
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.resources.KuntaRekryJobResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks.AbstractKuntaRekryJobTask;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryJobEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryJobTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryRemoveJobTask;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class KuntaRekryJobEntityUpdater extends EntityUpdater<AbstractKuntaRekryJobTask> {

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
  private KuntaRekryJobResourceContainer kuntaRekryJobResourceContainer;

  @Override
  public String getName() {
    return "organization-jobs";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  @Override
  public void execute(AbstractKuntaRekryJobTask task) {
    if (task instanceof KuntaRekryRemoveJobTask) {
      removeKuntaRekryJob((KuntaRekryRemoveJobTask) task);
    } else if (task instanceof KuntaRekryJobEntityTask) {
      updateKuntaRekryJob((KuntaRekryJobEntityTask) task); 
    }
  }
  
  private void executeNextTask() {
    AbstractKuntaRekryJobTask task = kuntaRekryJobTaskQueue.next();
    if (task != null) {
      execute(task);
    }
  }
  
  private void updateKuntaRekryJob(KuntaRekryJobEntityTask task) {
    KuntaRekryJob kuntaRekryJob = task.getEntity();
    OrganizationId organizationId = task.getOrganizationId();
    Long orderIndex = task.getOrderIndex();
    
    JobId kuntaRekryId = new JobId(organizationId, KuntaRekryConsts.IDENTIFIER_NAME, String.valueOf(kuntaRekryJob.getJobId())); 

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, kuntaRekryId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaRekryJob));
    
    JobId kuntaApiJobId = new JobId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    Job job = kuntaRekryTranslator.translateJob(kuntaApiJobId, kuntaRekryJob);
    
    kuntaRekryJobResourceContainer.put(kuntaApiJobId, job);
  }

  private void removeKuntaRekryJob(KuntaRekryRemoveJobTask task) {
    JobId kuntaRekryJobId = task.getKuntaRekryJobId();
    
    Identifier kuntaRekryJobIdentifier = identifierController.findIdentifierById(kuntaRekryJobId);
    if (kuntaRekryJobIdentifier != null) {
      modificationHashCache.clear(kuntaRekryJobIdentifier.getKuntaApiId());
      kuntaRekryJobResourceContainer.clear(kuntaRekryJobId);
      identifierController.deleteIdentifier(kuntaRekryJobIdentifier);
    }
  }

}
