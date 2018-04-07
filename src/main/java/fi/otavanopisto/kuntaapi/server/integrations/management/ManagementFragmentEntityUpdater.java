package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Fragment;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.FragmentIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.resources.FragmentResourceContainer;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementFragmentEntityUpdater extends EntityUpdater<IdTask<FragmentId>> {

  @Inject
  private Logger logger;

  @Inject
  private OrganizationSettingController organizationSettingController; 

  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private FragmentResourceContainer fragmentResourceContainer;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private FragmentIdTaskQueue fragmentIdTaskQueue;

  @Override
  public String getName() {
    return "management-fragments";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  @Override
  public void execute(IdTask<FragmentId> task) {
    if (task.getOperation() == Operation.UPDATE) {
      updateManagementFragment(task.getId(), task.getOrderIndex()); 
    } else if (task.getOperation() == Operation.REMOVE) {
      deleteManagementFragment(task.getId());
    }
  }
  
  private void executeNextTask() {
    IdTask<FragmentId> task = fragmentIdTaskQueue.next();
    if (task != null) {
      execute(task);
    }
  }
  
  private void updateManagementFragment(FragmentId fragmentId, Long orderIndex) {
    OrganizationId organizationId = fragmentId.getOrganizationId();
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
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

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, fragmentId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    FragmentId kuntaApiFragmentId = new FragmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Fragment fragment = managementTranslator.translateFragment(kuntaApiFragmentId, managementFragment);
    
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(fragment));
    fragmentResourceContainer.put(kuntaApiFragmentId, fragment);
  }

  private void deleteManagementFragment(FragmentId managementFragmentId) {
    OrganizationId organizationId = managementFragmentId.getOrganizationId();
    
    Identifier fragmentIdentifier = identifierController.findIdentifierById(managementFragmentId);
    if (fragmentIdentifier != null) {
      FragmentId kuntaApiFragmentId = new FragmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, fragmentIdentifier.getKuntaApiId());
      modificationHashCache.clear(fragmentIdentifier.getKuntaApiId());
      fragmentResourceContainer.clear(kuntaApiFragmentId);
      identifierController.deleteIdentifier(fragmentIdentifier);
    }
    
  }
}
