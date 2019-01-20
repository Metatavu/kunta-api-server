package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.ejb3.annotation.Pool;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.EnvironmentalWarningId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.index.IndexRequest;
import fi.metatavu.kuntaapi.server.index.IndexableEnvironmentalWarning;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.tasks.FmiWeatherAlertEntityTask;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.tasks.FmiWeatherAlertTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs.Feature;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.resources.EnvironmentalWarningResourceContainer;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.rest.model.EnvironmentalWarning;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsJob;
import fi.metatavu.kuntaapi.server.tasks.jms.JmsQueueProperties;

/**
 * Entity discover job for FMI weather alerts
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@SuppressWarnings ("squid:S3306")
@MessageDriven (
  activationConfig = {
    @ActivationConfigProperty (propertyName = JmsQueueProperties.DESTINATION_LOOKUP, propertyValue = FmiWeatherAlertTaskQueue.JMS_QUEUE),
    @ActivationConfigProperty(propertyName = JmsQueueProperties.MESSAGE_SELECTOR, propertyValue = JmsQueueProperties.TASK_MESSAGE_SELECTOR)
  }
)
@Pool(JmsQueueProperties.HIGH_CONCURRENCY_POOL)
public class FmiWeatherAlertsEntityDiscoverJob extends AbstractJmsJob<FmiWeatherAlertEntityTask> {

  @Inject
  private Logger logger;
  
  @Inject
  private FmiWeatherAlertsTranslator fmiWeatherAlertsTranslator;

  @Inject
  private OrganizationSettingController organizationSettingController;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;

  @Inject
  private FmiWeatherAlertsIdFactory fmiWeatherAlertsIdFactory;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private EnvironmentalWarningResourceContainer environmentalWarningResourceContainer;

  @Inject
  private Event<IndexRequest> indexRequest;
  
  @Override
  public void execute(FmiWeatherAlertEntityTask task) {
    Feature feature = task.getFeature();
    Long orderIndex = task.getOrderIndex();
    String featureIdentifier = feature.getProperties().getIdentifier();
    String featureReference = feature.getProperties().getReference();
    
    List<OrganizationId> organizationIds = organizationSettingController.listOrganizationIdsWithSetting(FmiWeatherAlertsConsts.ORGANIZATION_SETTING_REFERENCE);
    for (OrganizationId organizationId : organizationIds) {
      String organizationReference = organizationSettingController.getSettingValue(organizationId, FmiWeatherAlertsConsts.ORGANIZATION_SETTING_REFERENCE);
      if (StringUtils.equals(featureReference, organizationReference)) {
        EnvironmentalWarningId fmiId = fmiWeatherAlertsIdFactory.createEnvironmentalWarningId(organizationId, featureIdentifier);
        Identifier identifier = identifierController.acquireIdentifier(orderIndex, fmiId);
        identifierRelationController.setParentId(identifier, organizationId);
        EnvironmentalWarningId kuntaApiId = kuntaApiIdFactory.createFromIdentifier(EnvironmentalWarningId.class, identifier);
        EnvironmentalWarning environmentalWarning = fmiWeatherAlertsTranslator.translateWeatherAlert(kuntaApiId, feature);
        if (environmentalWarning != null) {
          modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(environmentalWarning));
          environmentalWarningResourceContainer.put(kuntaApiId, environmentalWarning);
          indexEnvironmentalWarning(organizationId, environmentalWarning, orderIndex);
        } else {
          if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("Failed to translate FMI Weather Alert %s", featureIdentifier));
          }
        }
      }
    }
  }

  /**
   * Indexes environmental warning
   * 
   * @param organizationId organization id
   * @param environmentalWarning environmental warning entity
   * @param orderIndex order index
   */
  private void indexEnvironmentalWarning(OrganizationId organizationId, EnvironmentalWarning environmentalWarning, Long orderIndex) {
    String descriptionEn = null;
    String descriptionFi = null;
    String descriptionSv = null;
    
    for (LocalizedValue description : environmentalWarning.getDescription()) {
      switch (description.getLanguage()) {
        case "en":
          descriptionEn = description.getValue();
        break;
        case "sv":
          descriptionSv = description.getValue();
        break;
        case "fi":
          descriptionFi = description.getValue();
        break;
      }
    }
    
    IndexableEnvironmentalWarning result = new IndexableEnvironmentalWarning();
    result.setActualizationProbability(environmentalWarning.getActualizationProbability());
    result.setCauses(environmentalWarning.getCauses());
    result.setContext(environmentalWarning.getContext());
    result.setDescriptionEn(descriptionEn);
    result.setDescriptionFi(descriptionFi);
    result.setDescriptionSv(descriptionSv);
    result.setEnd(environmentalWarning.getEnd());
    result.setEnvironmentalWarningId(environmentalWarning.getId());
    result.setOrderIndex(orderIndex);
    result.setSeverity(environmentalWarning.getSeverity());
    result.setStart(environmentalWarning.getStart());
    result.setType(environmentalWarning.getType());
    result.setOrganizationId(organizationId.getId());
    
    indexRequest.fire(new IndexRequest(result));
  }

}
