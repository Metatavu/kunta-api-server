package fi.metatavu.kuntaapi.server.integrations.kuntarekry;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.discover.IdUpdater;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.JobId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.GenericHttpClient;
import fi.metatavu.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryJobEntityTask;
import fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryJobTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryRemoveJobTask;
import fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks.OrganizationJobsTaskQueue;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.OrganizationEntityUpdateTask;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class KuntaRekryJobIdUpdater extends IdUpdater {
  
  @Inject
  private Logger logger;
  
  @Inject
  private GenericHttpClient httpClient;

  @Inject
  private IdController idController;

  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  @Inject
  private OrganizationJobsTaskQueue organizationJobsTaskQueue;
  
  @Inject
  private KuntaRekryJobTaskQueue kuntaRekryJobTaskQueue;
  
  @Override
  public String getName() {
    return "organization-jobs";
  }

  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationJobsTaskQueue.next();
    if (task != null) {
      updateOrganizationJobs(task.getOrganizationId());
    } else if (organizationJobsTaskQueue.isEmptyAndLocalNodeResponsible()) {
      organizationJobsTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(KuntaRekryConsts.ORGANIZATION_SETTING_APIURI));
    }
  }
  
  private void updateOrganizationJobs(OrganizationId organizationId) {
    String apiUri = organizationSettingController.getSettingValue(organizationId, KuntaRekryConsts.ORGANIZATION_SETTING_APIURI);
    if (StringUtils.isBlank(apiUri)) {
      return;
    }
    
    URI uri;
    try {
      uri = new URI(apiUri);
    } catch (URISyntaxException e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, String.format("Malformed URI %s", apiUri), e);
      }
      
      return;
    }
    
    List<JobId> existingKuntaRekryJobIds = idController.translateIds(identifierRelationController.listJobIdsBySourceAndParentId(KuntaRekryConsts.IDENTIFIER_NAME, organizationId), KuntaRekryConsts.IDENTIFIER_NAME);
    
    Response<List<KuntaRekryJob>> jobsResponse = httpClient.doGETRequest(uri, new GenericHttpClient.ResultType<List<KuntaRekryJob>>() {});
    if (jobsResponse.isOk()) {
      List<KuntaRekryJob> kuntaRekryJobs = jobsResponse.getResponseEntity();
      for (int i = 0; i < kuntaRekryJobs.size(); i++) {
        KuntaRekryJob kuntaRekryJob = kuntaRekryJobs.get(i);
        Long orderIndex = (long) i;
        kuntaRekryJobTaskQueue.enqueueTask(false, new KuntaRekryJobEntityTask(organizationId, kuntaRekryJob, orderIndex));
        JobId kuntaRekryId = new JobId(organizationId, KuntaRekryConsts.IDENTIFIER_NAME, String.valueOf(kuntaRekryJob.getJobId()));
        existingKuntaRekryJobIds.remove(kuntaRekryId);
      }
      
      for (JobId existingKuntaRekryJobId : existingKuntaRekryJobIds) {
        kuntaRekryJobTaskQueue.enqueueTask(false, new KuntaRekryRemoveJobTask(existingKuntaRekryJobId));
      }
      
    } else {
      logger.log(Level.SEVERE, () -> String.format("Failed to list jobs from Kuntarekry. API Returned [%d] %s", jobsResponse.getStatus(), jobsResponse.getMessage()));
    }
  }

}
