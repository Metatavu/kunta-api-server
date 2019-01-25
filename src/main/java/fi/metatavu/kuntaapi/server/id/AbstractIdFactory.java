package fi.metatavu.kuntaapi.server.id;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;

public abstract class AbstractIdFactory {

  @Inject
  private Logger logger;
  
  public abstract String getSource();

  public <T extends BaseId> T createFromIdentifier(Class<T> idClass, Identifier identifier) {
    if(identifier.getOrganizationKuntaApiId() == null) {
      return createId(idClass, null, identifier.getSourceId());
    } else {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      return createId(idClass, organizationId, identifier.getSourceId());
    }
  }
  
  public OrganizationId createOrganizationId(UUID id) {
    return createId(OrganizationId.class, null, id);
  }
  
  public OrganizationId createOrganizationId(String id) {
    return createId(OrganizationId.class, null, id);
  }

  public ServiceId createServiceId(UUID id) {
    return createId(ServiceId.class, null, id);
  }
  
  public ServiceId createServiceId(String id) {
    return createId(ServiceId.class, null, id);
  }

  public AttachmentId createAttachmentId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(AttachmentId.class, kuntaApiOrganizationId, id);
  }

  public AttachmentId createAttachmentId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(AttachmentId.class, kuntaApiOrganizationId, id);
  }

  public MenuId createMenuId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(MenuId.class, kuntaApiOrganizationId, id);
  }

  public MenuId createMenuId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(MenuId.class, kuntaApiOrganizationId, id);
  }

  public EventId createEventId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(EventId.class, kuntaApiOrganizationId, id);
  }

  public EventId createEventId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(EventId.class, kuntaApiOrganizationId, id);
  }

  public PageId createPageId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(PageId.class, kuntaApiOrganizationId, id);
  }

  public PageId createPageId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(PageId.class, kuntaApiOrganizationId, id);
  }
  
  public ShortlinkId createShortlinkId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(ShortlinkId.class, kuntaApiOrganizationId, id);
  }
  
  public ShortlinkId createShortlinkId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(ShortlinkId.class, kuntaApiOrganizationId, id);
  }

  public IncidentId createIncidentId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(IncidentId.class, kuntaApiOrganizationId, id);
  }
  
  public IncidentId createIncidentId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(IncidentId.class, kuntaApiOrganizationId, id);
  }
  
  public EmergencyId createEmergencyId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(EmergencyId.class, kuntaApiOrganizationId, id);
  }
  
  public EmergencyId createEmergencyId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(EmergencyId.class, kuntaApiOrganizationId, id);
  }
  
  /**
   * Creates EnvironmentalWarningId
   * 
   * @param kuntaApiOrganizationId organization id
   * @param id id
   * @return created id
   */
  public EnvironmentalWarningId createEnvironmentalWarningId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(EnvironmentalWarningId.class, kuntaApiOrganizationId, id);
  }

  /**
   * Creates EnvironmentalWarningId
   * 
   * @param kuntaApiOrganizationId organization id
   * @param id id
   * @return created id
   */
  public EnvironmentalWarningId createEnvironmentalWarningId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(EnvironmentalWarningId.class, kuntaApiOrganizationId, id);
  }
  
  public JobId createJobId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(JobId.class, kuntaApiOrganizationId, id);
  }
  
  public JobId createJobId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(JobId.class, kuntaApiOrganizationId, id);
  }
  
  public FragmentId createFragmentId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(FragmentId.class, kuntaApiOrganizationId, id);
  }
  
  public FragmentId createFragmentId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(FragmentId.class, kuntaApiOrganizationId, id);
  }
  
  public ElectronicServiceChannelId createElectronicServiceChannelId(UUID id) {
    return createId(ElectronicServiceChannelId.class, null, id);
  }
  
  public ElectronicServiceChannelId createElectronicServiceChannelId(String id) {
    return createId(ElectronicServiceChannelId.class, null, id);
  }
  
  public PrintableFormServiceChannelId createPrintableFormServiceChannelId(UUID id) {
    return createId(PrintableFormServiceChannelId.class, null, id);
  }
  
  public PrintableFormServiceChannelId createPrintableFormServiceChannelId(String id) {
    return createId(PrintableFormServiceChannelId.class, null, id);
  }
  
  public PhoneServiceChannelId createPhoneServiceChannelId(UUID id) {
    return createId(PhoneServiceChannelId.class, null, id);
  }
  
  public PhoneServiceChannelId createPhoneServiceChannelId(String id) {
    return createId(PhoneServiceChannelId.class, null, id);
  }
  
  public ServiceLocationServiceChannelId createServiceLocationServiceChannelId(UUID id) {
    return createId(ServiceLocationServiceChannelId.class, null, id);
  }
  
  public ServiceLocationServiceChannelId createServiceLocationServiceChannelId(String id) {
    return createId(ServiceLocationServiceChannelId.class, null, id);
  }
  
  public WebPageServiceChannelId createWebPageServiceChannelId(UUID id) {
    return createId(WebPageServiceChannelId.class, null, id);
  }
  
  public WebPageServiceChannelId createWebPageServiceChannelId(String id) {
    return createId(WebPageServiceChannelId.class, null, id);
  }

  public PublicTransportAgencyId createAgencyId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(PublicTransportAgencyId.class, kuntaApiOrganizationId, id);
  }

  public PublicTransportAgencyId createAgencyId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(PublicTransportAgencyId.class, kuntaApiOrganizationId, id);
  }

  public PublicTransportScheduleId createScheduleId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(PublicTransportScheduleId.class, kuntaApiOrganizationId, id);
  }

  public PublicTransportScheduleId createScheduleId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(PublicTransportScheduleId.class, kuntaApiOrganizationId, id);
  }

  public PublicTransportRouteId createRouteId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(PublicTransportRouteId.class, kuntaApiOrganizationId, id);
  }

  public PublicTransportRouteId createRouteId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(PublicTransportRouteId.class, kuntaApiOrganizationId, id);
  }
  
  public PublicTransportStopId createStopId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(PublicTransportStopId.class, kuntaApiOrganizationId, id);
  }
  
  public PublicTransportStopId createStopId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(PublicTransportStopId.class, kuntaApiOrganizationId, id);
  }
  
  public PublicTransportStopTimeId createStopTimeId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(PublicTransportStopTimeId.class, kuntaApiOrganizationId, id);
  }
  
  public PublicTransportStopTimeId createStopTimeId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(PublicTransportStopTimeId.class, kuntaApiOrganizationId, id);
  }
  
  public PublicTransportTripId createTripId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(PublicTransportTripId.class, kuntaApiOrganizationId, id);
  }
    
  public PublicTransportTripId createTripId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(PublicTransportTripId.class, kuntaApiOrganizationId, id);
  }
  
  public ContactId createContactId(OrganizationId kuntaApiOrganizationId, UUID id) {
    return createId(ContactId.class, kuntaApiOrganizationId, id);
  }
  
  public ContactId createContactId(OrganizationId kuntaApiOrganizationId, String id) {
    return createId(ContactId.class, kuntaApiOrganizationId, id);
  }

  public CodeId createCodeId(String id) {
    return createId(CodeId.class, null, id);
  }

  public <T extends BaseId> T createId(Class<T> idClass, OrganizationId organizationId, UUID id) {
    if (id == null) {
      return null;
    }
    
    return createId(idClass, organizationId, id.toString());
  }
  
  public <T extends BaseId> T createId(Class<T> idClass, OrganizationId organizationId, String id) {
    if (id == null) {
      return null;
    }
    
    if (isOrganizationBaseId(idClass)) {
      return createOrganizationBaseId(idClass, id, organizationId);
    } else {
      return createBaseId(idClass, id);
    }
  }
  
  private boolean isOrganizationBaseId(Class<? extends BaseId> idClass) {
    return OrganizationBaseId.class.isAssignableFrom(idClass);
  }
  
  private <T extends BaseId> T createOrganizationBaseId(Class<T> idClass, String id, OrganizationId organizationId) {
    Constructor<T> idConstructor;
    try {
      idConstructor = idClass.getDeclaredConstructor(OrganizationId.class, String.class, String.class);
      return idConstructor.newInstance(organizationId, getSource(), id);
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      logger.log(Level.SEVERE, "Failed to construct id", e);
    }
    
    return null;
  }
  
  private <T extends BaseId> T createBaseId(Class<T> idClass, String id) {
    if (id == null) {
      return null;
    }
    
    Constructor<T> idConstructor;
    try {
      idConstructor = idClass.getDeclaredConstructor(String.class, String.class);
      return idConstructor.newInstance(getSource(), id);
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      logger.log(Level.SEVERE, "Failed to construct id", e);
    }
    
    return null;
  }
  
}
