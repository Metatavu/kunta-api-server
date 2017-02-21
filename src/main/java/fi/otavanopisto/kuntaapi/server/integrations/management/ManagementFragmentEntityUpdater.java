package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Fragment;
import fi.otavanopisto.kuntaapi.server.cache.FragmentCache;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.FragmentIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

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
  private IdentifierRelationController identifierRelationController;

  @Inject
  private FragmentCache fragmentCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private FragmentIdTaskQueue fragmentIdTaskQueue;
  
  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "management-fragments";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Timeout
  public void timeout(Timer timer) {
    if (systemSettingController.isNotTestingOrTestRunning()) {
      executeNextTask();
    }
    
    startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
  }

  private void executeNextTask() {
    IdTask<FragmentId> task = fragmentIdTaskQueue.next();
    if (task != null) {
      if (task.getOperation() == Operation.UPDATE) {
        updateManagementFragment(task.getId(), task.getOrderIndex()); 
      } else if (task.getOperation() == Operation.REMOVE) {
        deleteManagementFragment(task.getId());
      }
    }
  }
  
  private void updateManagementFragment(FragmentId fragmentId, Long orderIndex) {
    OrganizationId organizationId = fragmentId.getOrganizationId();
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
      identifier = identifierController.createIdentifier(orderIndex, fragmentId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, orderIndex);
    }

    identifierRelationController.setParentId(identifier, organizationId);
    
    FragmentId kuntaApiFragmentId = new FragmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Fragment fragment = managementTranslator.translateFragment(kuntaApiFragmentId, managementFragment);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(fragment));
    fragmentCache.put(kuntaApiFragmentId, fragment);
  }

  private void deleteManagementFragment(FragmentId managementFragmentId) {
    OrganizationId organizationId = managementFragmentId.getOrganizationId();
    
    Identifier fragmentIdentifier = identifierController.findIdentifierById(managementFragmentId);
    if (fragmentIdentifier != null) {
      FragmentId kuntaApiFragmentId = new FragmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, fragmentIdentifier.getKuntaApiId());
      modificationHashCache.clear(fragmentIdentifier.getKuntaApiId());
      fragmentCache.clear(kuntaApiFragmentId);
      identifierController.deleteIdentifier(fragmentIdentifier);
    }
    
  }
}
