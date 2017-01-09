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
import fi.otavanopisto.kuntaapi.server.discover.IdUpdateRequestQueue;
import fi.otavanopisto.kuntaapi.server.discover.ServiceIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexableService;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;
import fi.otavanopisto.kuntaapi.server.utils.LocalizationUtils;
import fi.otavanopisto.restfulptv.client.ApiResponse;
import fi.otavanopisto.restfulptv.client.model.LocalizedListItem;
import fi.otavanopisto.restfulptv.client.model.Service;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  @Inject
  private Event<IndexRequest> indexRequest;

  private boolean stopped;
  private IdUpdateRequestQueue<ServiceIdUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = new IdUpdateRequestQueue<>(PtvConsts.IDENTIFIFER_NAME);
  }

  @Override
  public String getName() {
    return "services";
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
  
  public void onServiceIdUpdateRequest(@Observes ServiceIdUpdateRequest event) {
    if (!stopped) {
      if (!PtvConsts.IDENTIFIFER_NAME.equals(event.getId().getSource())) {
        return;
      }
      
      queue.add(event);
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      ServiceIdUpdateRequest updateRequest = queue.next();
      if (updateRequest != null) {
        updatePtvService(updateRequest);
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updatePtvService(ServiceIdUpdateRequest updateRequest) {
    updatePtvService(updateRequest.getId(), updateRequest.getOrderIndex());
  }
  
  private void updatePtvService(ServiceId serviceId, Long orderIndex) {
    ApiResponse<Service> response = ptvApi.getServicesApi().findService(serviceId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.findIdentifierById(serviceId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(serviceId, orderIndex, serviceId);
      } else {
        identifier = identifierController.updateIdentifier(identifier, serviceId, orderIndex);
      }
      
      Service service = response.getResponse();

      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(service));
      index(identifier.getKuntaApiId(), service);
    } else {
      logger.warning(String.format("Service %s processing failed on [%d] %s", serviceId.getId(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void index(String serviceId, Service service) {
    List<LocalizedListItem> descriptions = service.getDescriptions();
    List<LocalizedListItem> names = service.getNames();
    List<String> ptvOrganizationIds = service.getOrganizationIds();
    List<String> organizationIds = new ArrayList<>(ptvOrganizationIds.size());
    
    for (String ptvOrganizationId : ptvOrganizationIds) {
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(new OrganizationId(PtvConsts.IDENTIFIFER_NAME, ptvOrganizationId), KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId != null) {
        organizationIds.add(kuntaApiOrganizationId.getId());
      } else {
        logger.warning(String.format("Could not translate organization %s into Kunta API id", ptvOrganizationId));
      }
    }
    
    for (String language : LocalizationUtils.getListsLanguages(names, descriptions)) {
      IndexableService indexableService = new IndexableService();
      indexableService.setShortDescription(LocalizationUtils.getBestMatchingValue("ShortDescription", descriptions, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setDescription(LocalizationUtils.getBestMatchingValue("Description", descriptions, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setUserInstruction(LocalizationUtils.getBestMatchingValue("ServiceUserInstruction", descriptions, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setKeywords(service.getKeywords());
      indexableService.setLanguage(language);
      indexableService.setName(LocalizationUtils.getBestMatchingValue("Name", names, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setAlternativeName(LocalizationUtils.getBestMatchingValue("AlternativeName", names, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setServiceId(serviceId);
      indexableService.setOrganizationIds(organizationIds);
      
      indexRequest.fire(new IndexRequest(indexableService));
    }
    
  }
  

}
