package fi.otavanopisto.kuntaapi.server.integrations.management;

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
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Fragment;
import fi.otavanopisto.kuntaapi.server.cache.FragmentCache;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.FragmentIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.FragmentIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdateRequestQueue;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementFragmentEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private FragmentCache fragmentCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private IdUpdateRequestQueue<FragmentIdUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = new IdUpdateRequestQueue<>(ManagementConsts.IDENTIFIER_NAME);
  }

  @Override
  public String getName() {
    return "management-fragments";
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
  public void onFragmentIdUpdateRequest(@Observes FragmentIdUpdateRequest event) {
    if (!stopped) {
      FragmentId fragmentId = event.getId();
      
      if (!StringUtils.equals(fragmentId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      queue.add(event);
    }
  }
  
  @Asynchronous
  public void onFragmentIdRemoveRequest(@Observes FragmentIdRemoveRequest event) {
    if (!stopped) {
      FragmentId fragmentId = event.getId();
      
      if (!StringUtils.equals(fragmentId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      deleteFragment(event, fragmentId);
    }
  }
  
  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning()) {
        FragmentIdUpdateRequest updateRequest = queue.next();
        if (updateRequest != null) {
          updateManagementFragment(updateRequest.getOrganizationId(), updateRequest.getId(), updateRequest.getOrderIndex());
        }
      }
      
      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateManagementFragment(OrganizationId organizationId, FragmentId fragmentId, Long orderIndex) {
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Fragment> response = api.wpV2FragmentIdGet(fragmentId.getId(), null, null, null);
    if (response.isOk()) {
      updateManagementFragment(organizationId, response.getResponse(), orderIndex);
    } else {
      logger.warning(String.format("Find organization %s fragment %s failed on [%d] %s", organizationId.getId(), fragmentId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementFragment(OrganizationId organizationId, Fragment managementFragment, Long orderIndex) {
    FragmentId fragmentId = new FragmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementFragment.getId()));

    Identifier identifier = identifierController.findIdentifierById(fragmentId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(organizationId, orderIndex, fragmentId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, organizationId, orderIndex);
    }
    
    FragmentId kuntaApiFragmentId = new FragmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Fragment fragment = managementTranslator.translateFragment(kuntaApiFragmentId, managementFragment);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(fragment));
    fragmentCache.put(kuntaApiFragmentId, fragment);
  }

  private void deleteFragment(FragmentIdRemoveRequest event, FragmentId managementFragmentId) {
    OrganizationId organizationId = event.getOrganizationId();
    
    Identifier fragmentIdentifier = identifierController.findIdentifierById(managementFragmentId);
    if (fragmentIdentifier != null) {
      FragmentId kuntaApiFragmentId = new FragmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, fragmentIdentifier.getKuntaApiId());
      queue.remove(managementFragmentId);
      modificationHashCache.clear(fragmentIdentifier.getKuntaApiId());
      fragmentCache.clear(kuntaApiFragmentId);
      identifierController.deleteIdentifier(fragmentIdentifier);
    }
    
  }
}
