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
import fi.otavanopisto.kuntaapi.server.integrations.tpt.client.model.DocsEntry;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.resources.TptJobResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.tasks.TptJobEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.tasks.TptJobTaskQueue;
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
public class TptEntityUpdater extends EntityUpdater<TptJobEntityTask> {

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
  private TptJobTaskQueue tptJobTaskQueue;

  @Override
  public String getName() {
    return "tpt-entities";
  }

  @Override
  public void timeout() {
    TptJobEntityTask task = tptJobTaskQueue.next();
    if (task != null) {
      execute(task);
    }
  }
  
  @Override
  public void execute(TptJobEntityTask task) {
    DocsEntry tptJob = task.getTptJob();
    OrganizationId kuntaApiOrganizationId = task.getKuntaApiOrganizationId();
    JobId tptJobId = tptIdFactory.createJobId(kuntaApiOrganizationId, tptJob.getId());
    Long orderIndex = task.getOrderIndex();
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, tptJobId);
    JobId kuntaApiJobId = kuntaApiIdFactory.createFromIdentifier(JobId.class, identifier);
    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    fi.metatavu.kuntaapi.server.rest.model.Job kuntaApiJob = tptTranslator.translateJob(kuntaApiJobId, tptJob);

    if (kuntaApiJob != null) {
      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiJob));
      tptJobResourceContainer.put(kuntaApiJobId, kuntaApiJob);
    } else {
      logger.severe(String.format("Failed to translate tpt job %s", identifier.getKuntaApiId()));
    }
  }
  
}
