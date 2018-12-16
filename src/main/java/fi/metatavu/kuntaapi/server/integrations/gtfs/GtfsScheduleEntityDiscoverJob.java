package fi.metatavu.kuntaapi.server.integrations.gtfs;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.ejb3.annotation.Pool;
import org.onebusaway.gtfs.model.ServiceCalendar;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PublicTransportScheduleId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.gtfs.resources.GtfsPublicTransportScheduleResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.gtfs.tasks.GtfsScheduleEntityTask;
import fi.metatavu.kuntaapi.server.integrations.gtfs.tasks.GtfsScheduleTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsJob;
import fi.metatavu.kuntaapi.server.tasks.jms.JmsQueueProperties;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
@MessageDriven (
  activationConfig = {
    @ActivationConfigProperty (propertyName = JmsQueueProperties.DESTINATION_LOOKUP, propertyValue = GtfsScheduleTaskQueue.JMS_QUEUE),
    @ActivationConfigProperty(propertyName = JmsQueueProperties.MESSAGE_SELECTOR, propertyValue = JmsQueueProperties.TASK_MESSAGE_SELECTOR)
  }
)
@Pool(JmsQueueProperties.HIGH_CONCURRENCY_POOL)
public class GtfsScheduleEntityDiscoverJob extends AbstractJmsJob<GtfsScheduleEntityTask> {

  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;

  @Inject
  private GtfsTranslator gtfsTranslator;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private GtfsPublicTransportScheduleResourceContainer gtfsPublicTransportScheduleCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory; 

  @Inject
  private GtfsIdFactory gtfsIdFactory;
  
  @Override
  public void execute(GtfsScheduleEntityTask task) {
    updateGtfsSchedule(task);
  }
  
  private void updateGtfsSchedule(GtfsScheduleEntityTask task) {
    ServiceCalendar gtfsServiceCalendar = task.getEntity();
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(task.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.SEVERE, () -> String.format("Could not translate organization %s into Kunta API id", task.getOrganizationId()));
      return;
    }
    
    Long orderIndex = task.getOrderIndex();
    PublicTransportScheduleId gtfsScheduleId = gtfsIdFactory.createScheduleId(kuntaApiOrganizationId, gtfsServiceCalendar.getServiceId().getId());

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, gtfsScheduleId);
    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    PublicTransportScheduleId kuntaApiScheduleId = kuntaApiIdFactory.createFromIdentifier(PublicTransportScheduleId.class, identifier);
    fi.metatavu.kuntaapi.server.rest.model.Schedule kuntaApiSchedule = gtfsTranslator.translateSchedule(kuntaApiScheduleId, gtfsServiceCalendar, task.getExceptions());
    if (kuntaApiSchedule != null) {
      modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(kuntaApiSchedule));
      gtfsPublicTransportScheduleCache.put(kuntaApiScheduleId, kuntaApiSchedule);
    } else {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate gtfs schedule %s", identifier.getKuntaApiId()));
    }
  }

}
