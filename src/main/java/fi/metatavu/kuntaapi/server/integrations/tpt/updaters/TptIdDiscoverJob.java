package fi.metatavu.kuntaapi.server.integrations.tpt.updaters;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;
import fi.metatavu.kuntaapi.server.id.JobId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.metatavu.kuntaapi.server.integrations.tpt.TptConsts;
import fi.metatavu.kuntaapi.server.integrations.tpt.TptIdFactory;
import fi.metatavu.kuntaapi.server.integrations.tpt.client.TptApi;
import fi.metatavu.kuntaapi.server.integrations.tpt.client.model.ApiResponse;
import fi.metatavu.kuntaapi.server.integrations.tpt.client.model.DocsEntry;
import fi.metatavu.kuntaapi.server.integrations.tpt.tasks.TptJobRemoveTask;
import fi.metatavu.kuntaapi.server.integrations.tpt.tasks.TptJobTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.tpt.tasks.TptJobUpdateTask;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;

/**
 * Id updater for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class TptIdDiscoverJob extends IdDiscoverJob {
  
  @Inject
  private Logger logger;

  @Inject
  private TptApi tptApi;
  
  @Inject
  private TptJobTaskQueue tptJobTaskQueue;

  @Inject
  private OrganizationSettingController organizationSettingController;

  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private TptIdFactory tptIdFactory;

  @Override
  public String getName() {
    return "tpt-ids";
  }
  
  @Override
  public void timeout() {
    updateTptEntities();
  }
  
  /**
   * Iterates over all organizations having te-palvelut.fi -integration enabled and 
   * enqueues tasks for all found and removed jobs
   */
  private void updateTptEntities() {
    List<OrganizationId> organizationIds = organizationSettingController.listOrganizationIdsWithSetting(TptConsts.ORGANIZATION_SETTING_AREA);
    for (OrganizationId organizationId : organizationIds) {
      Response<ApiResponse> response = tptApi.searchByArea(organizationId);
      if (response.isOk()) {
        ApiResponse apiResponse = response.getResponseEntity();
        Integer headerStatus = apiResponse.getResponseHeader().getStatus();
        int status = headerStatus == null ? -1 : headerStatus.intValue();

        if (apiResponse.getResponse() == null) {
          logger.warning(() -> String.format("Listing organization %s tpt jobs list returned null", organizationId.getId()));
        } else if (status != 0) {
          logger.warning(() -> String.format("Listing organization %s tpt jobs list returned status %d", organizationId.getId(), status));
        } else {
          enqueueTasks(organizationId, apiResponse.getResponse());
        }
      } else {
        logger.warning(() -> String.format("Listing organization %s tpt jobs failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
      }
    }
  }
  
  private void enqueueTasks(OrganizationId organizationId, fi.metatavu.kuntaapi.server.integrations.tpt.client.model.Response response) {
    long orderIndex = 0;
    
    List<JobId> removedJobIds = identifierController.listOrganizationJobIdsBySource(organizationId, TptConsts.IDENTIFIER_NAME);
    
    for (DocsEntry docsEntry : response.getDocs()) {
      JobId jobId = tptIdFactory.createJobId(organizationId, docsEntry.getId());
      removedJobIds.remove(jobId);
      tptJobTaskQueue.enqueueTask(new TptJobUpdateTask(false, organizationId, docsEntry, orderIndex));
      orderIndex++;
    }
    
    for (JobId removedJobId : removedJobIds) {
      tptJobTaskQueue.enqueueTask(new TptJobRemoveTask(true, removedJobId));
    }
  }
  
}