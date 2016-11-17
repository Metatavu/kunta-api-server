package fi.otavanopisto.kuntaapi.server.id;

import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

/**
 * Controller for ids
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class IdController {
  
  @Inject
  private Instance<IdProvider> idProviders;
  
  @SuppressWarnings ("squid:MethodCyclomaticComplexity")
  public BaseId translateId(BaseId id, String target) {
    if (id == null) {
      return id;
    }
    
    if (id.getSource().equals(target)) {
      return id;
    }
    
    switch (id.getType()) {
      case ORGANIZATION:
        return translateOrganizationId((OrganizationId) id, target);
      case SERVICE:
        return translateServiceId((ServiceId) id, target);
      case ORGANIZATION_SERVICE:
        return translateOrganizationServiceId((OrganizationServiceId) id, target);
      case ELECTRONIC_SERVICE_CHANNEL:
        return translateElectronicServiceChannelId((ElectronicServiceChannelId) id, target);
      case PHONE_CHANNEL:
        return translatePhoneServiceChannelId((PhoneChannelId) id, target);
      case PRINTABLE_FORM_CHANNEL:
        return translatePrintableFormServiceChannelId((PrintableFormChannelId) id, target);
      case SERVICE_LOCATION_CHANNEL:
        return translateServiceLocationChannelId((ServiceLocationChannelId) id, target);
      case WEBPAGE_CHANNEL:
        return translateWebPageServiceChannelId((WebPageChannelId) id, target);
      case EVENT:
        return translateEventId((EventId) id, target);
      case ATTACHMENT:
        return translateAttachmentId((AttachmentId) id, target);
      case NEWS_ARTICLE:
        return translateNewsArticleId((NewsArticleId) id, target);
      case BANNER:
        return translateBannerId((BannerId) id, target);
      case TILE:
        return translateTileId((TileId) id, target);
      case PAGE:
        return translatePageId((PageId) id, target);
      case FILE:
        return translateFileId((FileId) id, target);
      case MENU:
        return translateMenuId((MenuId) id, target);
      case MENU_ITEM:
        return translateMenuItemId((MenuItemId) id, target);
      case JOB:
        return translateJobId((JobId) id, target);
      case ANNOUNCEMENT:
        return translateAnnouncementId((AnnouncementId) id, target);
       default:
        return null;
    }
    
  }
  
  /**
   * Translates organization id into into target id
   * 
   * @param organizationId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public OrganizationId translateOrganizationId(OrganizationId organizationId, String target) {
    if (organizationId == null) {
      return null;
    }
    
    if (StringUtils.equals(organizationId.getSource(), target)) {
      return organizationId;
    }
    
    IdProvider idProvider = getIdProvider(organizationId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(organizationId, target);
    }
    
    return null;
  }

  /**
   * Translates service id into into target id
   * 
   * @param serviceId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public ServiceId translateServiceId(ServiceId serviceId, String target) {
    if (serviceId == null) {
      return null;
    }
    
    if (StringUtils.equals(serviceId.getSource(), target)) {
      return serviceId;
    }
    
    IdProvider idProvider = getIdProvider(serviceId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(serviceId, target);
    }
    
    return null;
  }
  
  /**
   * Translates organization service id into into target id
   * 
   * @param organizationServiceId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public OrganizationServiceId translateOrganizationServiceId(OrganizationServiceId organizationServiceId, String target) {
    if (organizationServiceId == null) {
      return null;
    }

    if (StringUtils.equals(organizationServiceId.getSource(), target)) {
      return organizationServiceId;
    }
    
    IdProvider idProvider = getIdProvider(organizationServiceId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(organizationServiceId, target);
    }
    
    return null;
  }

  /**
   * Translates electornic service channel id into into target id
   * 
   * @param serviceChannelId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public ElectronicServiceChannelId translateElectronicServiceChannelId(ElectronicServiceChannelId serviceChannelId, String target) {
    if (serviceChannelId == null) {
      return null;
    }

    if (StringUtils.equals(serviceChannelId.getSource(), target)) {
      return serviceChannelId;
    }
    
    IdProvider idProvider = getIdProvider(serviceChannelId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(serviceChannelId, target);
    }
    
    return null;
  }

  /**
   * Translates phone service channel id into into target id
   * 
   * @param serviceChannelId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public PhoneChannelId translatePhoneServiceChannelId(PhoneChannelId serviceChannelId, String target) {
    if (serviceChannelId == null) {
      return null;
    }

    if (StringUtils.equals(serviceChannelId.getSource(), target)) {
      return serviceChannelId;
    }
    
    IdProvider idProvider = getIdProvider(serviceChannelId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(serviceChannelId, target);
    }
    
    return null;
  }

  /**
   * Translates printable form service channel id into into target id
   * 
   * @param serviceChannelId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public PrintableFormChannelId translatePrintableFormServiceChannelId(PrintableFormChannelId serviceChannelId, String target) {
    if (serviceChannelId == null) {
      return null;
    }

    if (StringUtils.equals(serviceChannelId.getSource(), target)) {
      return serviceChannelId;
    }
    
    IdProvider idProvider = getIdProvider(serviceChannelId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(serviceChannelId, target);
    }
    
    return null;
  }

  /**
   * Translates service location channel id into into target id
   * 
   * @param serviceChannelId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public ServiceLocationChannelId translateServiceLocationChannelId(ServiceLocationChannelId serviceChannelId, String target) {
    if (serviceChannelId == null) {
      return null;
    }

    if (StringUtils.equals(serviceChannelId.getSource(), target)) {
      return serviceChannelId;
    }
    
    IdProvider idProvider = getIdProvider(serviceChannelId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(serviceChannelId, target);
    }
    
    return null;
  }

  /**
   * Translates webpage service channel id into into target id
   * 
   * @param serviceChannelId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public WebPageChannelId translateWebPageServiceChannelId(WebPageChannelId serviceChannelId, String target) {
    if (serviceChannelId == null) {
      return null;
    }

    if (StringUtils.equals(serviceChannelId.getSource(), target)) {
      return serviceChannelId;
    }
    
    IdProvider idProvider = getIdProvider(serviceChannelId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(serviceChannelId, target);
    }
    
    return null;
  }
  
  /**
   * Translates event class id into into target id
   * 
   * @param eventId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public EventId translateEventId(EventId eventId, String target) {
    if (eventId == null) {
      return null;
    }

    if (StringUtils.equals(eventId.getSource(), target)) {
      return eventId;
    }
    
    IdProvider idProvider = getIdProvider(eventId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(eventId, target);
    }
    
    return null;
  }
  
  /**
   * Translates news article id into into target id
   * 
   * @param newsArticleId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public NewsArticleId translateNewsArticleId(NewsArticleId newsArticleId, String target) {
    if (newsArticleId == null) {
      return null;
    }

    if (StringUtils.equals(newsArticleId.getSource(), target)) {
      return newsArticleId;
    }
    
    IdProvider idProvider = getIdProvider(newsArticleId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(newsArticleId, target);
    }
    
    return null;
  }
  
  /**
   * Translates event class id into into target id
   * 
   * @param attachmentId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public AttachmentId translateAttachmentId(AttachmentId attachmentId, String target) {
    if (attachmentId == null) {
      return null;
    }

    if (StringUtils.equals(attachmentId.getSource(), target)) {
      return attachmentId;
    }
    
    IdProvider idProvider = getIdProvider(attachmentId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(attachmentId, target);
    }
    
    return null;
  }
  
  /**
   * Translates banner id into into target id
   * 
   * @param bannerId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public BannerId translateBannerId(BannerId bannerId, String target) {
    if (bannerId == null) {
      return null;
    }

    if (StringUtils.equals(bannerId.getSource(), target)) {
      return bannerId;
    }
    
    IdProvider idProvider = getIdProvider(bannerId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(bannerId, target);
    }
    
    return null;
  }
  
  /**
   * Translates tile id into into target id
   * 
   * @param tileId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public TileId translateTileId(TileId tileId, String target) {
    if (tileId == null) {
      return null;
    }

    if (StringUtils.equals(tileId.getSource(), target)) {
      return tileId;
    }
    
    IdProvider idProvider = getIdProvider(tileId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(tileId, target);
    }
    
    return null;
  }
  
  /**
   * Translates file id into into target id
   * 
   * @param fileId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public FileId translateFileId(FileId fileId, String target) {
    if (fileId == null) {
      return null;
    }

    if (StringUtils.equals(fileId.getSource(), target)) {
      return fileId;
    }
    
    IdProvider idProvider = getIdProvider(fileId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(fileId, target);
    }
    
    return null;
  }
  
  /**
   * Translates menu id into into target id
   * 
   * @param menuId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public MenuId translateMenuId(MenuId menuId, String target) {
    if (menuId == null) {
      return null;
    }

    if (StringUtils.equals(menuId.getSource(), target)) {
      return menuId;
    }
    
    IdProvider idProvider = getIdProvider(menuId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(menuId, target);
    }
    
    return null;
  }
  
  /**
   * Translates menuItem id into into target id
   * 
   * @param menuItemId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public MenuItemId translateMenuItemId(MenuItemId menuItemId, String target) {
    if (menuItemId == null) {
      return null;
    }

    if (StringUtils.equals(menuItemId.getSource(), target)) {
      return menuItemId;
    }
    
    IdProvider idProvider = getIdProvider(menuItemId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(menuItemId, target);
    }
    
    return null;
  }
  
  /**
   * Translates job id into into target id
   * 
   * @param jobId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public JobId translateJobId(JobId jobId, String target) {
    if (jobId == null) {
      return null;
    }

    if (StringUtils.equals(jobId.getSource(), target)) {
      return jobId;
    }
    
    IdProvider idProvider = getIdProvider(jobId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(jobId, target);
    }
    
    return null;
  }
  
  /**
   * Translates announcement id into into target id
   * 
   * @param announcementId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public AnnouncementId translateAnnouncementId(AnnouncementId announcementId, String target) {
    if (announcementId == null) {
      return null;
    }

    if (StringUtils.equals(announcementId.getSource(), target)) {
      return announcementId;
    }
    
    IdProvider idProvider = getIdProvider(announcementId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(announcementId, target);
    }
    
    return null;
  }
  
  /**
   * Translates page id into into target id
   * 
   * @param pageId id to be translated
   * @param target target
   * @return translated id or null if translation has failed
   */
  public PageId translatePageId(PageId pageId, String target) {
    if (pageId == null) {
      return null;
    }

    if (StringUtils.equals(pageId.getSource(), target)) {
      return pageId;
    }
    
    IdProvider idProvider = getIdProvider(pageId.getSource(), target);
    if (idProvider != null) {
      return idProvider.translate(pageId, target);
    }
    
    return null;
  }
  
  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(AttachmentId id1, AttachmentId id2) {
    AttachmentId kuntaApiId1 = translateAttachmentId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    AttachmentId kuntaApiId2 = translateAttachmentId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }

  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(EventId id1, EventId id2) {
    EventId kuntaApiId1 = translateEventId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    EventId kuntaApiId2 = translateEventId(id2, KuntaApiConsts.IDENTIFIER_NAME);
 
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }

  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(NewsArticleId id1, NewsArticleId id2) {
    NewsArticleId kuntaApiId1 = translateNewsArticleId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    NewsArticleId kuntaApiId2 = translateNewsArticleId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }

  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(OrganizationId id1, OrganizationId id2) {
    OrganizationId kuntaApiId1 = translateOrganizationId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    OrganizationId kuntaApiId2 = translateOrganizationId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }
  
  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(ElectronicServiceChannelId id1, ElectronicServiceChannelId id2) {
    ElectronicServiceChannelId kuntaApiId1 = translateElectronicServiceChannelId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    ElectronicServiceChannelId kuntaApiId2 = translateElectronicServiceChannelId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }

  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(PhoneChannelId id1, PhoneChannelId id2) {
    PhoneChannelId kuntaApiId1 = translatePhoneServiceChannelId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    PhoneChannelId kuntaApiId2 = translatePhoneServiceChannelId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }

  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(PrintableFormChannelId id1, PrintableFormChannelId id2) {
    PrintableFormChannelId kuntaApiId1 = translatePrintableFormServiceChannelId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    PrintableFormChannelId kuntaApiId2 = translatePrintableFormServiceChannelId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }

  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(ServiceLocationChannelId id1, ServiceLocationChannelId id2) {
    ServiceLocationChannelId kuntaApiId1 = translateServiceLocationChannelId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    ServiceLocationChannelId kuntaApiId2 = translateServiceLocationChannelId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }

  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(WebPageChannelId id1, WebPageChannelId id2) {
    WebPageChannelId kuntaApiId1 = translateWebPageServiceChannelId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    WebPageChannelId kuntaApiId2 = translateWebPageServiceChannelId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }

  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(OrganizationServiceId id1, OrganizationServiceId id2) {
    OrganizationServiceId kuntaApiId1 = translateOrganizationServiceId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    OrganizationServiceId kuntaApiId2 = translateOrganizationServiceId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }

  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(ServiceId id1, ServiceId id2) {
    ServiceId kuntaApiId1 = translateServiceId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    ServiceId kuntaApiId2 = translateServiceId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }

  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(BannerId id1, BannerId id2) {
    BannerId kuntaApiId1 = translateBannerId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    BannerId kuntaApiId2 = translateBannerId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }

  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(TileId id1, TileId id2) {
    TileId kuntaApiId1 = translateTileId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    TileId kuntaApiId2 = translateTileId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }
  
  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(FileId id1, FileId id2) {
    FileId kuntaApiId1 = translateFileId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    FileId kuntaApiId2 = translateFileId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }
  
  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(MenuId id1, MenuId id2) {
    MenuId kuntaApiId1 = translateMenuId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    MenuId kuntaApiId2 = translateMenuId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }
  
  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(MenuItemId id1, MenuItemId id2) {
    MenuItemId kuntaApiId1 = translateMenuItemId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    MenuItemId kuntaApiId2 = translateMenuItemId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }
  
  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(JobId id1, JobId id2) {
    JobId kuntaApiId1 = translateJobId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    JobId kuntaApiId2 = translateJobId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }
  
  /**
   * Translates both ids into Kunta Api ids and check whether they match
   * 
   * @param id1 id1
   * @param id2 id2
   * @return whether ids match
   */
  public boolean idsEqual(PageId id1, PageId id2) {
    PageId kuntaApiId1 = translatePageId(id1, KuntaApiConsts.IDENTIFIER_NAME);
    PageId kuntaApiId2 = translatePageId(id2, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId1 == null || kuntaApiId2 == null) {
      return false;
    }
    
    return kuntaApiId1.equals(kuntaApiId2);
  }
  
  private IdProvider getIdProvider(String source, String target) {
    Iterator<IdProvider> iterator = idProviders.iterator();
    while (iterator.hasNext()) {
      IdProvider idProvider = iterator.next();
      if (idProvider.canTranslate(source, target)) {
        return idProvider;
      }
    }
    
    return null;
  }
  
}
