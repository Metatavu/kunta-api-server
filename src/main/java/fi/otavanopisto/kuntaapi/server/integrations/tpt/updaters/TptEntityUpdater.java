package fi.otavanopisto.kuntaapi.server.integrations.tpt.updaters;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.TptIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.TptTranslator;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.client.TptApi;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.client.model.DocsEntry;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.resources.TptJobResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.tasks.TptAbstractJobTask;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.tasks.TptJobRemoveTask;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.tasks.TptJobTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.tasks.TptJobUpdateTask;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

/**
 * Entity update for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class TptEntityUpdater extends EntityUpdater<TptAbstractJobTask> {

  @Inject
  private Logger logger;
  
  @Inject
  private TptTranslator tptTranslator;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private TptJobResourceContainer tptJobResourceContainer;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory; 

  @Inject
  private TptIdFactory tptIdFactory;

  @Inject
  private TptApi tptApi;
  
  @Inject
  private TptJobTaskQueue tptJobTaskQueue;

  @Override
  public String getName() {
    return "tpt-entities";
  }

  @Override
  public void timeout() {
    TptAbstractJobTask task = tptJobTaskQueue.next();
    if (task != null) {
      execute(task);
    }
  }

  @Override
  public void execute(TptAbstractJobTask task) {
    if (task instanceof TptJobRemoveTask) {
      executeRemove((TptJobRemoveTask) task);
    } else if (task instanceof TptJobUpdateTask) {
      executeUpdate((TptJobUpdateTask) task);
    }
  }
  
  private void executeUpdate(TptJobUpdateTask task) {
    DocsEntry tptJob = task.getTptJob();
    OrganizationId kuntaApiOrganizationId = task.getKuntaApiOrganizationId();
    JobId tptJobId = tptIdFactory.createJobId(kuntaApiOrganizationId, tptJob.getId());
    Long orderIndex = task.getOrderIndex();
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, tptJobId);
    JobId kuntaApiJobId = kuntaApiIdFactory.createFromIdentifier(JobId.class, identifier);
    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    String baseUrl = tptApi.getBaseUrl();
    fi.metatavu.kuntaapi.server.rest.model.Job kuntaApiJob = tptTranslator.translateJob(kuntaApiJobId, baseUrl, tptJob);

    if (kuntaApiJob != null) {
      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiJob));
      tptJobResourceContainer.put(kuntaApiJobId, kuntaApiJob);
    } else {
      logger.severe(() -> String.format("Failed to translate tpt job %s", identifier.getKuntaApiId()));
    }
  }
  
  private void executeRemove(TptJobRemoveTask task) {
    JobId removedTptJobId = task.getRemovedTptJobId();
    
    Identifier jobIdentifier = identifierController.findIdentifierById(removedTptJobId);
    if (jobIdentifier != null) {
      JobId removedKuntaApiJobId = kuntaApiIdFactory.createFromIdentifier(JobId.class, jobIdentifier);
      modificationHashCache.clear(jobIdentifier.getKuntaApiId());
      tptJobResourceContainer.clear(removedKuntaApiJobId);
      identifierController.deleteIdentifier(jobIdentifier);
    }  
  }
  
}