package fi.metatavu.kuntaapi.server.integrations.kuntarekry;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.ejb3.annotation.Pool;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.JobId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.kuntarekry.resources.KuntaRekryJobResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks.AbstractKuntaRekryJobTask;
import fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryJobEntityTask;
import fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryJobTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryRemoveJobTask;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsJob;
import fi.metatavu.kuntaapi.server.tasks.jms.JmsQueueProperties;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
@MessageDriven (
  activationConfig = {
    @ActivationConfigProperty (propertyName = JmsQueueProperties.DESTINATION_LOOKUP, propertyValue = KuntaRekryJobTaskQueue.JMS_QUEUE),
    @ActivationConfigProperty(propertyName = JmsQueueProperties.MESSAGE_SELECTOR, propertyValue = JmsQueueProperties.TASK_MESSAGE_SELECTOR)
  }
)
@Pool(JmsQueueProperties.LOW_CONCURRENCY_POOL)
public class KuntaRekryJobEntityDiscoverJob extends AbstractJmsJob<AbstractKuntaRekryJobTask> {

  @Inject
  private KuntaRekryTranslator kuntaRekryTranslator;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private KuntaRekryJobResourceContainer kuntaRekryJobResourceContainer;

  @Override
  public void execute(AbstractKuntaRekryJobTask task) {
    if (task instanceof KuntaRekryRemoveJobTask) {
      removeKuntaRekryJob((KuntaRekryRemoveJobTask) task);
    } else if (task instanceof KuntaRekryJobEntityTask) {
      updateKuntaRekryJob((KuntaRekryJobEntityTask) task); 
    }
  }
  
  private void updateKuntaRekryJob(KuntaRekryJobEntityTask task) {
    KuntaRekryJob kuntaRekryJob = task.getEntity();
    OrganizationId organizationId = task.getOrganizationId();
    Long orderIndex = task.getOrderIndex();
    
    JobId kuntaRekryId = new JobId(organizationId, KuntaRekryConsts.IDENTIFIER_NAME, String.valueOf(kuntaRekryJob.getUrl())); 

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
