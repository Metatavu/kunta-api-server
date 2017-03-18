package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryJobEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks.KuntaRekryJobTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks.OrganizationJobsTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;

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
  private OrganizationSettingController organizationSettingController;
  
  @Inject
  private OrganizationJobsTaskQueue organizationJobsTaskQueue;
  
  @Inject
  private KuntaRekryJobTaskQueue kuntaRekryJobTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "organization-jobs";
  }

  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationJobsTaskQueue.next();
    if (task != null) {
      updateOrganizationJobs(task.getOrganizationId());
    } else {
      organizationJobsTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(KuntaRekryConsts.ORGANIZATION_SETTING_APIURI));
    }
  }
  
  @Override
  public TimerService geTimerService() {
    return timerService;
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
    
    Response<List<KuntaRekryJob>> jobsResponse = httpClient.doGETRequest(uri, new GenericHttpClient.ResultType<List<KuntaRekryJob>>() {});
    if (jobsResponse.isOk()) {
      List<KuntaRekryJob> kuntaRekryJobs = jobsResponse.getResponseEntity();
      for (int i = 0; i < kuntaRekryJobs.size(); i++) {
        KuntaRekryJob kuntaRekryJob = kuntaRekryJobs.get(i);
        Long orderIndex = (long) i;
        kuntaRekryJobTaskQueue.enqueueTask(false, new KuntaRekryJobEntityTask(organizationId, kuntaRekryJob, orderIndex));
      }
    } else {
      logger.log(Level.SEVERE, () -> String.format("Failed to list jobs from Kuntarekry. API Returned [%d] %s", jobsResponse.getStatus(), jobsResponse.getMessage()));
    }
  }

}
