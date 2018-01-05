package fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.updaters;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.EmergencyId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.TilannehuoneConsts;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.TilannehuoneIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.TilannehuoneTranslator;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.model.Emergency;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.resources.TilannehuoneEmergencyResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.tasks.TilannehuoneEmergencyEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.tasks.TilannehuoneEmergencyTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class TilannehuoneEntityUpdater extends EntityUpdater<TilannehuoneEmergencyEntityTask> {

  @Inject
  private Logger logger;
  
  @Inject
  private TilannehuoneTranslator tilannehuoneTranslator;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private TilannehuoneEmergencyResourceContainer tilannehuoneEmergencyResourceContainer;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory; 

  @Inject
  private TilannehuoneIdFactory tilannehuoneIdFactory;
  
  @Inject
  private TilannehuoneEmergencyTaskQueue tilannehuoneEmergencyTaskQueue;

  @Override
  public String getName() {
    return "tilannehuone-entities";
  }

  @Override
  public void timeout() {
    TilannehuoneEmergencyEntityTask task = tilannehuoneEmergencyTaskQueue.next();
    if (task != null) {
      execute(task);
    }
  }
  
  @Override
  public void execute(TilannehuoneEmergencyEntityTask task) {
    Emergency tilannehuoneEmergency = task.getTilannehuoneEmergency();
    
    if (StringUtils.isNotBlank(tilannehuoneEmergency.getArea())) {
      OrganizationId kuntaApiOrganizationId = organizationSettingController.findOrganizationIdByKeyAndValue(TilannehuoneConsts.ORGANIZATION_SETTING_AREA, tilannehuoneEmergency.getArea());
      if (kuntaApiOrganizationId != null) {
        EmergencyId tilannehuoneEmergencyId = tilannehuoneIdFactory.createEmergencyId(kuntaApiOrganizationId, tilannehuoneEmergency.getId());
        Long orderIndex = task.getOrderIndex();
        Identifier identifier = identifierController.acquireIdentifier(orderIndex, tilannehuoneEmergencyId);
        EmergencyId kuntaApiEmergencyId = kuntaApiIdFactory.createFromIdentifier(EmergencyId.class, identifier);
        identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
        
        fi.metatavu.kuntaapi.server.rest.model.Emergency kuntaApiEmergency = tilannehuoneTranslator.translateEmergency(kuntaApiEmergencyId, tilannehuoneEmergency);
        
        if (kuntaApiEmergency != null) {
          modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiEmergency));
          tilannehuoneEmergencyResourceContainer.put(kuntaApiEmergencyId, kuntaApiEmergency);
        } else {
          logger.severe(String.format("Failed to translate tilannehuone emergency %s", identifier.getKuntaApiId()));
        }
      }
    }
  }
}
