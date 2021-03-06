package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.integrations.IdMapProvider;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementPageIdMapResourceContainer;

@ApplicationScoped
public class ManagementIdMapProvider implements IdMapProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;
  
  @Inject
  private ManagementPageIdMapResourceContainer managementPageIdMapResourceContainer;
  
  @Override
  public BaseId findMappedPageParentId(OrganizationId organizationId, PageId pageId) {
    PageId kuntaApiPageId = idController.translatePageId(pageId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiPageId == null) {
      logger.log(Level.WARNING, () -> String.format("Could not translate page %s into kunta api", pageId));
      return null;  
    }
    
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.WARNING, () -> String.format("Could not translate organization %s into kunta api", organizationId));
      return null;  
    }
    
    Map<BaseId, BaseId> pageIdMap = managementPageIdMapResourceContainer.get(kuntaApiOrganizationId);
    if (pageIdMap == null) {
      return null;
    }
    
    return pageIdMap.get(kuntaApiPageId);
  }
   
}
