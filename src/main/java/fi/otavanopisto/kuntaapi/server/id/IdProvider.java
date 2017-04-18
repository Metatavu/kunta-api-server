package fi.otavanopisto.kuntaapi.server.id;

/**
 * Id translator provider interface
 * 
 * @author Otavan Opisto
 */
public interface IdProvider {

  /**
   * Returns whether provider can translate from given source to given target
   * 
   * @param source id source type
   * @param target id target type
   * @return whether provider can translate from given source to given target
   */
  public boolean canTranslate(String source, String target);
  
  /**
   * Translate organization id
   * 
   * @param organizationId original id
   * @param target target type
   * @return translated organization id
   */
  public OrganizationId translate(OrganizationId organizationId, String target);
  
  /**
   * Translate service id
   * 
   * @param serviceId original id
   * @param target target type
   * @return translated service id
   */
  public ServiceId translate(ServiceId serviceId, String target);
  
  /**
   * Translate electronic service channel id
   * 
   * @param serviceChannelId original id
   * @param target target type
   * @return translated id
   */
  public ElectronicServiceChannelId translate(ElectronicServiceChannelId serviceChannelId, String target);
  
  /**
   * Translate phone service channel id
   * 
   * @param serviceChannelId original id
   * @param target target type
   * @return translated id
   */
  public PhoneServiceChannelId translate(PhoneServiceChannelId serviceChannelId, String target);
  
  /**
   * Translate printable form service channel id
   * 
   * @param serviceChannelId original id
   * @param target target type
   * @return translated id
   */
  public PrintableFormServiceChannelId translate(PrintableFormServiceChannelId serviceChannelId, String target);
  
  /**
   * Translate service location channel id
   * 
   * @param serviceChannelId original id
   * @param target target type
   * @return translated id
   */
  public ServiceLocationServiceChannelId translate(ServiceLocationServiceChannelId serviceChannelId, String target);
  
  /**
   * Translate web page service channel id
   * 
   * @param serviceChannelId original id
   * @param target target type
   * @return translated id
   */
  public WebPageServiceChannelId translate(WebPageServiceChannelId serviceChannelId, String target);
  
  /**
   * Translate event id
   * 
   * @param eventId original id
   * @param target target type
   * @return translated id
   */
  public EventId translate(EventId eventId, String target);
  
  /**
   * Translate news article id
   * 
   * @param newsArticleId original id
   * @param target target type
   * @return translated id
   */
  public NewsArticleId translate(NewsArticleId newsArticleId, String target);
  
  /**
   * Translate banner id
   * 
   * @param bannerId original id
   * @param target target type
   * @return translated id
   */
  public BannerId translate(BannerId bannerId, String target);
  
  /**
   * Translate tile id
   * 
   * @param tileId original id
   * @param target target type
   * @return translated id
   */
  public TileId translate(TileId tileId, String target);

  /**
   * Translate attachment id
   * 
   * @param attachmentId original id
   * @param target target type
   * @return translated id
   */
  public AttachmentId translate(AttachmentId attachmentId, String target);

  /**
   * Translate page id
   * 
   * @param pageId original id
   * @param target target type
   * @return translated id
   */
  public PageId translate(PageId pageId, String target);


  /**
   * Translate menu id
   * 
   * @param menuId original id
   * @param target target type
   * @return translated id
   */
  public MenuId translate(MenuId menuId, String target);

  /**
   * Translate file id
   * 
   * @param fileId original id
   * @param target target type
   * @return translated id
   */
  public FileId translate(FileId fileId, String target);

  /**
   * Translate menuItem id
   * 
   * @param menuItemId original id
   * @param target target type
   * @return translated id
   */
  public MenuItemId translate(MenuItemId menuItemId, String target);

  /**
   * Translate job id
   * 
   * @param jobId original id
   * @param target target type
   * @return translated id
   */
  public JobId translate(JobId jobId, String target);
  
  /**
   * Translate announcement id
   * 
   * @param announcementId original id
   * @param target target type
   * @return translated id
   */
  public AnnouncementId translate(AnnouncementId announcementId, String target);
  
  /**
   * Translate contact id
   * 
   * @param contactId original id
   * @param target target type
   * @return translated id
   */
  public ContactId translate(ContactId contactId, String target);
  
  /**
   * Translate fragment id
   * 
   * @param fragmentId original id
   * @param target target type
   * @return translated id
   */
  public FragmentId translate(FragmentId fragmentId, String target);
  
  /**
   * Translate public transport agency id
   * 
   * @param publicTransportAgencyId original id
   * @param target target type
   * @return translated id
   */
  public PublicTransportAgencyId translate(PublicTransportAgencyId publicTransportAgencyId, String target);
  
  /**
   * Translate public transport schedule id
   * 
   * @param publicTransportScheduleId original id
   * @param target target type
   * @return translated id
   */
  public PublicTransportScheduleId translate(PublicTransportScheduleId publicTransportScheduleId, String target);
  
  /**
   * Translate public transport route id
   * 
   * @param publicTransportRouteId original id
   * @param target target type
   * @return translated id
   */
  public PublicTransportRouteId translate(PublicTransportRouteId publicTransportRouteId, String target);
  
  /**
   * Translate public transport stop id
   * 
   * @param publicTransportStopId original id
   * @param target target type
   * @return translated id
   */
  public PublicTransportStopId translate(PublicTransportStopId publicTransportStopId, String target);
  
  /**
   * Translate public transport stopTime id
   * 
   * @param publicTransportStopTimeId original id
   * @param target target type
   * @return translated id
   */
  public PublicTransportStopTimeId translate(PublicTransportStopTimeId publicTransportStopTimeId, String target);
  
  /**
   * Translate public transport trip id
   * 
   * @param publicTransportTripId original id
   * @param target target type
   * @return translated id
   */
  public PublicTransportTripId translate(PublicTransportTripId publicTransportTripId, String target);

  /**
   * Translate shortlink id
   * 
   * @param shortlinkId original id
   * @param target target type
   * @return translated id
   */
  public ShortlinkId translate(ShortlinkId shortlinkId, String target);

}
