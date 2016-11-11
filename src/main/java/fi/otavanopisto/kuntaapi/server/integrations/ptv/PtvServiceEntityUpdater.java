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
import fi.otavanopisto.kuntaapi.server.discover.ServiceIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexableService;
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
  private IdentifierController identifierController;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  @Inject
  private Event<IndexRequest> indexRequest;

  private boolean stopped;
  private List<ServiceId> queue;

  @PostConstruct
  public void init() {
    queue = new ArrayList<>();
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
        updatePtvService(queue.remove(0));          
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updatePtvService(ServiceId serviceId) {
    ApiResponse<Service> response = ptvApi.getServicesApi().findService(serviceId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.findIdentifierById(serviceId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(serviceId);
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
      indexRequest.fire(new IndexRequest(indexableService));
    }
    
  }
  

}
