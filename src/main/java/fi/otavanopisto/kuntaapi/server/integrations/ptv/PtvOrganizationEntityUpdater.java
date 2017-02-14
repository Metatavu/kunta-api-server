package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdateRequestQueue;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveOrganization;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexableOrganization;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
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
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdController idController;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private Event<IndexRequest> indexRequest;
  
  @Inject
  private Event<IndexRemoveRequest> indexRemoveRequest;

  @Resource
  private TimerService timerService;

  private boolean stopped;
  private IdUpdateRequestQueue<OrganizationIdUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = new IdUpdateRequestQueue<>(PtvConsts.IDENTIFIFER_NAME);
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
  
  @Asynchronous
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      if (!PtvConsts.IDENTIFIFER_NAME.equals(event.getId().getSource())) {
        return;
      }
      
      queue.add(event);
    }
  }
  
  @Asynchronous
  public void onOrganizationIdRemoveRequest(@Observes (during = TransactionPhase.BEFORE_COMPLETION) OrganizationIdRemoveRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getId();
      
      if (!StringUtils.equals(organizationId.getSource(), PtvConsts.IDENTIFIFER_NAME)) {
        return;
      }
      
      deleteOrganization(organizationId);
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning()) {
        OrganizationIdUpdateRequest updateRequest = queue.next();
        if (updateRequest != null) {
          updateOrganization(updateRequest);
        }
      }

      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateOrganization(OrganizationIdUpdateRequest updateRequest) {
    updateOrganization(updateRequest.getId(), updateRequest.getOrderIndex());
  }

  private void updateOrganization(OrganizationId organizationId, Long orderIndex) {
    ApiResponse<Organization> response = ptvApi.getOrganizationApi().findOrganization(organizationId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.findIdentifierById(organizationId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(orderIndex, organizationId);
      } else {
        identifier = identifierController.updateIdentifier(identifier, orderIndex);
      }
      
      Organization organization = response.getResponse();
      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(organization));
      
      index(identifier.getKuntaApiId(), organization);
    } else {
      logger.warning(String.format("Organization %s processing failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void deleteOrganization(OrganizationId organizationId) {
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);

    // Remove from index
    removeFromIndex(kuntaApiOrganizationId);
    
    // Remove from modification cache
    modificationHashCache.clear(kuntaApiOrganizationId.getId());
    
    Identifier identifier = identifierController.findIdentifierById(kuntaApiOrganizationId);
    if (identifier != null) {
      identifierController.deleteIdentifier(identifier);
    }
  }

  private void removeFromIndex(OrganizationId kuntaApiOrganizationId) {
    
    IndexRemoveOrganization indexRemoveOrganization = new IndexRemoveOrganization();
    indexRemoveOrganization.setLanguage("fi");
    indexRemoveOrganization.setOrganizationId(kuntaApiOrganizationId.getId());
    
    indexRemoveRequest.fire(new IndexRemoveRequest(indexRemoveOrganization));
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
