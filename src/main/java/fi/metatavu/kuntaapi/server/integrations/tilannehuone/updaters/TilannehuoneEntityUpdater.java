package fi.metatavu.kuntaapi.server.integrations.tilannehuone.updaters;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.discover.EntityUpdater;
import fi.metatavu.kuntaapi.server.id.EmergencyId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.index.IndexRequest;
import fi.metatavu.kuntaapi.server.index.IndexableEmergency;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.tilannehuone.TilannehuoneConsts;
import fi.metatavu.kuntaapi.server.integrations.tilannehuone.TilannehuoneIdFactory;
import fi.metatavu.kuntaapi.server.integrations.tilannehuone.TilannehuoneTranslator;
import fi.metatavu.kuntaapi.server.integrations.tilannehuone.model.Emergency;
import fi.metatavu.kuntaapi.server.integrations.tilannehuone.resources.TilannehuoneEmergencyResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.tilannehuone.tasks.TilannehuoneEmergencyEntityTask;
import fi.metatavu.kuntaapi.server.integrations.tilannehuone.tasks.TilannehuoneEmergencyTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;

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
  
  @Inject
  private Event<IndexRequest> indexRequest;

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
          IndexableEmergency indexableEmergency = createIndexableEmergency(kuntaApiOrganizationId, kuntaApiEmergencyId, kuntaApiEmergency.getLocation(), kuntaApiEmergency.getDescription(), kuntaApiEmergency.getTime(), orderIndex);
          indexRequest.fire(new IndexRequest(indexableEmergency));
        } else {
          logger.severe(() -> String.format("Failed to translate tilannehuone emergency %s", identifier.getKuntaApiId()));
        }
      }
    }
  }
  
  /**
   * Creates an indeable emergecy
   * 
   * @param kuntaApiOrganizationId kunta api organization id
   * @param kuntaApiEmergencyId kunta api emergency id
   * @param location location
   * @param description description
   * @param time emergency time
   * @param orderIndex order index
   * @return indeable emergency
   */
  private IndexableEmergency createIndexableEmergency(OrganizationId kuntaApiOrganizationId, EmergencyId kuntaApiEmergencyId, String location, String description, OffsetDateTime time, Long orderIndex) {
    IndexableEmergency result = new IndexableEmergency();
    result.setDescription(description);
    result.setEmergencyId(kuntaApiEmergencyId.getId());
    result.setLocation(location);
    result.setOrderIndex(orderIndex);
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setTime(time);
    return result;
  }
  
}
