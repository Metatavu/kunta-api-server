package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexableOrganization;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.Organization;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvOrganizationEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private Event<IndexRequest> indexRequest;

  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<OrganizationId> queue;

  @PostConstruct
  public void init() {
    queue = new ArrayList<>();
  }

  @Override
  public String getName() {
    return "organizations";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    stopped = false;
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      if (!PtvConsts.IDENTIFIFER_NAME.equals(event.getId().getSource())) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(event.getId());
        queue.add(0, event.getId());
      } else {
        if (!queue.contains(event.getId())) {
          queue.add(event.getId());
        }
      }
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        updateOrganization(queue.remove(0));
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateOrganization(OrganizationId organizationId) {
    ApiResponse<Organization> response = ptvApi.getOrganizationApi().findOrganization(organizationId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.findIdentifierById(organizationId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(organizationId);
      }
      
      Organization organization = response.getResponse();
      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(organization));
      
      index(identifier.getKuntaApiId(), organization);
    } else {
      logger.warning(String.format("Organization %s processing failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void index(String organizationId, Organization organization) {
    IndexableOrganization indexableOrganization = new IndexableOrganization();
    indexableOrganization.setBusinessCode(organization.getBusinessCode());
    indexableOrganization.setBusinessName(organization.getBusinessName());
    indexableOrganization.setLanguage("fi");
    indexableOrganization.setOrganizationId(organizationId);
    
    indexRequest.fire(new IndexRequest(indexableOrganization));
  }

}
