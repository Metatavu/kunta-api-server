package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.Organization;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class PtvOrganizationIdUpdater extends IdUpdater {

  private static final long BATCH_SIZE = 20;
  
  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private Event<TaskRequest> taskRequest;

  @Resource
  private TimerService timerService;

  private long offset;
  
  @PostConstruct
  public void init() {
    offset = 0;
  }
  
  @Override
  public String getName() {
    return "organizations";
  }
  
  @Override
  public void timeout() {
    discoverIds();
  }
  
  @Override
  public TimerService geTimerService() {
    return timerService;
  }

  private void discoverIds() {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    ApiResponse<List<Organization>> organizationsResponse = ptvApi.getOrganizationApi().listOrganizations(offset, BATCH_SIZE);
    if (!organizationsResponse.isOk()) {
      logger.severe(String.format("Organization list reported [%d] %s", organizationsResponse.getStatus(), organizationsResponse.getMessage()));
    } else {
      List<Organization> organizations = organizationsResponse.getResponse();
      for (int i = 0; i < organizations.size(); i++) {
        Organization organization = organizations.get(i);
        Long orderIndex = (long) i + offset;
        OrganizationId organizationId = new OrganizationId(PtvConsts.IDENTIFIER_NAME, organization.getId());
        boolean priority = identifierController.findIdentifierById(organizationId) == null;
        taskRequest.fire(new TaskRequest(priority, new IdTask<OrganizationId>(Operation.UPDATE, organizationId, orderIndex)));
      }
      
      if (organizations.size() == BATCH_SIZE) {
        offset += BATCH_SIZE;
      } else {
        offset = 0;
      }
    }
  }

}
