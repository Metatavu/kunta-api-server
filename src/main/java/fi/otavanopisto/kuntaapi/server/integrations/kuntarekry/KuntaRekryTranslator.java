package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.id.JobId;

@ApplicationScoped
public class KuntaRekryTranslator {
  
  public Job translateJob(JobId kuntaApiJobId, KuntaRekryJob kuntaRekryJob) {
    Job result = new Job();
    result.setId(kuntaApiJobId.getId());
    result.setTitle(kuntaRekryJob.getJobTitle());
    result.setDescription(kuntaRekryJob.getJobDescription());
    result.setEmploymentType(kuntaRekryJob.getEmploymentType());
    result.setLocation(kuntaRekryJob.getLocation());
    result.setOrganisationalUnit(kuntaRekryJob.getOrganisationalUnit());
    result.setDuration(kuntaRekryJob.getEmploymentDuration());
    result.setTaskArea(kuntaRekryJob.getTaskArea());
    result.setPublicationEnd(kuntaRekryJob.getPublicationTimeEnd());
    result.setPublicationStart(kuntaRekryJob.getPublicationTimeStart());
    result.setLink(kuntaRekryJob.getUrl());
    
    return result;
  }
  
}
