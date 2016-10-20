package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpCache;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient;
import fi.otavanopisto.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

@Dependent
public class KuntaRekryClient {

  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;

  @Inject
  private GenericHttpClient httpClient;
  
  @Inject
  private GenericHttpCache httpCache;
  
  @Inject
  private OrganizationSettingController organizationSettingController;

  public KuntaRekryJob findJob(OrganizationId organizationId, JobId jobId) {
    JobId kuntaRekryId = idController.translateJobId(jobId, KuntaRekryConsts.IDENTIFIER_NAME);
    if (kuntaRekryId == null) {
      logger.log(Level.WARNING, String.format("Could not translate jobId %s to kuntaRekryId", jobId.toString()));
      return null;
    }
    
    List<KuntaRekryJob> jobs = listJobs(organizationId);
    for (KuntaRekryJob job : jobs) {
      if (kuntaRekryId.getId().equals(String.valueOf(job.getJobId()))) {
        return job;
      }
    }
    
    return null;
  }
  
  public List<KuntaRekryJob> listJobs(OrganizationId organizationId) {
    try {
      String apiUri = organizationSettingController.getSettingValue(organizationId, KuntaRekryConsts.ORGANIZATION_SETTING_APIURI);
      if (StringUtils.isBlank(apiUri)) {
        return Collections.emptyList();
      }
      
      URI uri = new URI(apiUri);

      Response<List<KuntaRekryJob>> response = httpCache.get(KuntaRekryConsts.CACHE_NAME, uri, new GenericHttpClient.ResultType<Response<List<KuntaRekryJob>>>() {});
      if (response != null) {
        return response.getResponseEntity();
      } else {
        return Collections.emptyList();
      }
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, "Invalid Kuntarekry uri configured", e);
    }
    
    return Collections.emptyList();
  }

  public void refreshJobs(OrganizationId organizationId) {
    String apiUri = organizationSettingController.getSettingValue(organizationId, KuntaRekryConsts.ORGANIZATION_SETTING_APIURI);
    if (StringUtils.isBlank(apiUri)) {
      return;
    }
    
    try {
      URI uri = new URI(apiUri);
      
      Response<List<KuntaRekryJob>> jobsResponse = httpClient.doGETRequest(uri, new GenericHttpClient.ResultType<List<KuntaRekryJob>>() {});
      if (jobsResponse.isOk()) {
        httpCache.put(KuntaRekryConsts.CACHE_NAME, uri, jobsResponse);
      } else {
        logger.warning(String.format("Failed to refresh jobs from Kuntarekry. API Returned [%d] %s", jobsResponse.getStatus(), jobsResponse.getMessage()));
      }
    } catch (URISyntaxException e) {
      logger.log(Level.SEVERE, "Invalid Kuntarekry uri configured", e);
    }

  }
  
}
