package fi.otavanopisto.kuntaapi.server.integrations;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.otavanopisto.kuntaapi.server.id.IdProvider;
import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationBaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.PhoneChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationChannelId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.id.WebPageChannelId;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

@SuppressWarnings ("squid:S3306")
public abstract class AbstractIdProvider implements IdProvider {

  @Inject
  private Logger logger;
  
  @Inject
  private IdentifierController identifierController;
  
  public abstract String getSource();
  public abstract boolean isSupportedType(IdType type);
  
  @Override
  public boolean canTranslate(String source, String target) {
    if (getSource().equals(source) && KuntaApiConsts.IDENTIFIER_NAME.equals(target)) {
      return true;
    }
    
    if (getSource().equals(target) && KuntaApiConsts.IDENTIFIER_NAME.equals(source)) {
      return true;
    }
    
    return false;
  }

  @Override
  public OrganizationId translate(OrganizationId organizationId, String target) {
    return translateId(organizationId, IdType.ORGANIZATION, OrganizationId.class, target);
  }

  @Override
  public ServiceId translate(ServiceId serviceId, String target) {
    return translateId(serviceId, IdType.SERVICE, ServiceId.class, target);
  }
  
  @Override
  public ElectronicServiceChannelId translate(ElectronicServiceChannelId serviceChannelId, String target) {
    return translateId(serviceChannelId, IdType.ELECTRONIC_SERVICE_CHANNEL, ElectronicServiceChannelId.class, target);
  }
  
  @Override
  public PhoneChannelId translate(PhoneChannelId serviceChannelId, String target) {
    return translateId(serviceChannelId, IdType.PHONE_CHANNEL, PhoneChannelId.class, target);
  }
  
  @Override
  public PrintableFormChannelId translate(PrintableFormChannelId serviceChannelId, String target) {
    return translateId(serviceChannelId, IdType.PRINTABLE_FORM_CHANNEL, PrintableFormChannelId.class, target);
  }
  
  @Override
  public ServiceLocationChannelId translate(ServiceLocationChannelId serviceChannelId, String target) {
    return translateId(serviceChannelId, IdType.SERVICE_LOCATION_CHANNEL, ServiceLocationChannelId.class, target);
  }
  
  @Override
  public WebPageChannelId translate(WebPageChannelId serviceChannelId, String target) {
    return translateId(serviceChannelId, IdType.WEBPAGE_CHANNEL, WebPageChannelId.class, target);
  }
  
  @Override
  public OrganizationServiceId translate(OrganizationServiceId organizationServiceId, String target) {
    return translateId(organizationServiceId, IdType.ORGANIZATION_SERVICE, OrganizationServiceId.class, target);
  }
  
  @Override
  public EventId translate(EventId eventId, String target) {
    return translateId(eventId, IdType.EVENT, EventId.class, target);
  }

  @Override
  public NewsArticleId translate(NewsArticleId newsArticleId, String target) {
    return translateId(newsArticleId, IdType.NEWS_ARTICLE, NewsArticleId.class, target);
  }

  @Override
  public AttachmentId translate(AttachmentId attachmentId, String target) {
    return translateId(attachmentId, IdType.ATTACHMENT, AttachmentId.class, target);
  }

  @Override
  public BannerId translate(BannerId bannerId, String target) {
    return translateId(bannerId, IdType.BANNER, BannerId.class, target);
  }

  @Override
  public TileId translate(TileId tileId, String target) {
    return translateId(tileId, IdType.TILE, TileId.class, target);
  }

  @Override
  public PageId translate(PageId pageId, String target) {
    return translateId(pageId, IdType.PAGE, PageId.class, target);
  }

  @Override
  public MenuId translate(MenuId menuId, String target) {
    return translateId(menuId, IdType.MENU, MenuId.class, target);
  }

  @Override
  public FileId translate(FileId fileId, String target) {
    return translateId(fileId, IdType.FILE, FileId.class, target);
  }

  @Override
  public MenuItemId translate(MenuItemId menuItemId, String target) {
    return translateId(menuItemId, IdType.MENU_ITEM, MenuItemId.class, target);
  }

  @Override
  public JobId translate(JobId jobId, String target) {
    return translateId(jobId, IdType.JOB, JobId.class, target);
  }

  @Override
  public AnnouncementId translate(AnnouncementId announcementId, String target) {
    return translateId(announcementId, IdType.ANNOUNCEMENT, AnnouncementId.class, target);
  }

  @Override
  public ContactId translate(ContactId contactId, String target) {
    return translateId(contactId, IdType.CONTACT, ContactId.class, target);
  }

  @Override
  public FragmentId translate(FragmentId fragmentId, String target) {
    return translateId(fragmentId, IdType.FRAGMENT, FragmentId.class, target);
  }

  private <T extends BaseId> T translateId(T id, IdType type, Class<T> idClass, String target) {
    if (!isSupportedType(type)) {
      return null;
    }
    
    if (id.getSource().equals(target)) {
      return id;
    }

    if (toKuntaApi(id, target)) {
      return translateToKuntaApiId(id, idClass);
    } else if (toSourceId(id, target)) {
      return translateToSourceId(id, type, idClass);
    }
    
    return null;
  }
  
  private boolean toKuntaApi(BaseId baseId, String target) {
    return getSource().equals(baseId.getSource()) && KuntaApiConsts.IDENTIFIER_NAME.equals(target);
  }
  
  private boolean toSourceId(BaseId baseId, String target) {
    return target.equals(getSource()) && KuntaApiConsts.IDENTIFIER_NAME.equals(baseId.getSource());
  }

  private <T extends BaseId> T translateToSourceId(T id, IdType type, Class<T> idClass) {
    Identifier identifier;
    identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(type, getSource(), id.getId());
    if (identifier != null) {
      if (isOrganizationBaseId(idClass)) {
        return createOrganizationBaseId(idClass, getSource(), identifier.getSourceId(), identifier.getOrganizationKuntaApiId());
      } else {
        return createBaseId(idClass, getSource(), identifier.getSourceId());
      }
    }
    
    return null;
  }

  private <T extends BaseId> T translateToKuntaApiId(T id, Class<T> idClass) {
    Identifier identifier;
    identifier = identifierController.findIdentifierById(id);
    if (identifier != null) {
      if (isOrganizationBaseId(idClass)) {
        return createOrganizationBaseId(idClass, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId(), identifier.getOrganizationKuntaApiId()); 
      } else {
        return createBaseId(idClass, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    }
    
    return null;
  }
  
  private boolean isOrganizationBaseId(Class<? extends BaseId> idClass) {
    return OrganizationBaseId.class.isAssignableFrom(idClass);
  }
  private <T extends BaseId> T createOrganizationBaseId(Class<T> idClass, String source, String id, String organizationKuntaApiId) {
    Constructor<T> idConstructor;
    try {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, organizationKuntaApiId);
      idConstructor = idClass.getDeclaredConstructor(OrganizationId.class, String.class, String.class);
      return idConstructor.newInstance(organizationId, source, id);
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      logger.log(Level.SEVERE, "Failed to construct id", e);
    }
    
    return null;
  }
  
  private <T extends BaseId> T createBaseId(Class<T> idClass, String source, String id) {
    Constructor<T> idConstructor;
    try {
      idConstructor = idClass.getDeclaredConstructor(String.class, String.class);
      return idConstructor.newInstance(source, id);
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      logger.log(Level.SEVERE, "Failed to construct id", e);
    }
    
    return null;
  }
  
  
}
