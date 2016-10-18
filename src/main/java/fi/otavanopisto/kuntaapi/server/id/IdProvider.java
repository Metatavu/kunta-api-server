package fi.otavanopisto.kuntaapi.server.id;

import fi.otavanopisto.kuntaapi.server.integrations.FileId;

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
  public PhoneChannelId translate(PhoneChannelId serviceChannelId, String target);
  
  /**
   * Translate printable form service channel id
   * 
   * @param serviceChannelId original id
   * @param target target type
   * @return translated id
   */
  public PrintableFormChannelId translate(PrintableFormChannelId serviceChannelId, String target);
  
  /**
   * Translate service location channel id
   * 
   * @param serviceChannelId original id
   * @param target target type
   * @return translated id
   */
  public ServiceLocationChannelId translate(ServiceLocationChannelId serviceChannelId, String target);
  
  /**
   * Translate web page service channel id
   * 
   * @param serviceChannelId original id
   * @param target target type
   * @return translated id
   */
  public WebPageChannelId translate(WebPageChannelId serviceChannelId, String target);
  
  /**
   * Translate organization service id
   * 
   * @param organizationServiceId original id
   * @param target target type
   * @return translated id
   */
  public OrganizationServiceId translate(OrganizationServiceId organizationServiceId, String target);
  
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

}
