package fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentId;
import fi.otavanopisto.kuntaapi.server.integrations.BannerId;
import fi.otavanopisto.kuntaapi.server.integrations.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.EventId;
import fi.otavanopisto.kuntaapi.server.integrations.FileId;
import fi.otavanopisto.kuntaapi.server.integrations.IdProvider;
import fi.otavanopisto.kuntaapi.server.integrations.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.MenuId;
import fi.otavanopisto.kuntaapi.server.integrations.MenuItemId;
import fi.otavanopisto.kuntaapi.server.integrations.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.PhoneChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.PrintableFormChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceLocationChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.WebPageChannelId;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

/**
 * Id provider for Mikkeli Nyt
 * 
 * @author Antti Leppä
 */
@Dependent
public class MikkeliNytIdProvider implements IdProvider {
  
  @Inject
  private IdentifierController identifierController;

  private MikkeliNytIdProvider() {
  }
  
  @Override
  public boolean canTranslate(String source, String target) {
    if (MikkeliNytConsts.IDENTIFIER_NAME.equals(source) && KuntaApiConsts.IDENTIFIER_NAME.equals(target)) {
      return true;
    }
    
    if (MikkeliNytConsts.IDENTIFIER_NAME.equals(target) && KuntaApiConsts.IDENTIFIER_NAME.equals(source)) {
      return true;
    }
    
    return false;
  }

  @Override
  public OrganizationId translate(OrganizationId organizationId, String target) {
    return null;
  }

  @Override
  public ServiceId translate(ServiceId serviceId, String target) {
    return null;
  }
  
  @Override
  public ElectronicServiceChannelId translate(ElectronicServiceChannelId serviceChannelId, String target) {
    return null;
  }
  
  @Override
  public PhoneChannelId translate(PhoneChannelId serviceChannelId, String target) {
    return null;
  }
  
  @Override
  public PrintableFormChannelId translate(PrintableFormChannelId serviceChannelId, String target) {
    return null;
  }
  
  @Override
  public ServiceLocationChannelId translate(ServiceLocationChannelId serviceChannelId, String target) {
    return null;
  }
  
  @Override
  public WebPageChannelId translate(WebPageChannelId serviceChannelId, String target) {
    return null;
  }
  
  @Override
  public OrganizationServiceId translate(OrganizationServiceId organizationServiceId, String target) {
    return null;
  }

  @Override
  public EventId translate(EventId eventId, String target) {
    Identifier identifier;
    
    if (MikkeliNytConsts.IDENTIFIER_NAME.equals(eventId.getSource())) {
      identifier = identifierController.findIdentifierById(eventId);
      if (identifier != null) {
        return new EventId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(eventId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.EVENT, MikkeliNytConsts.IDENTIFIER_NAME, eventId.getId());
      if (identifier != null) {
        return new EventId(MikkeliNytConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public AttachmentId translate(AttachmentId attachmentId, String target) {
    Identifier identifier;
    
    if (MikkeliNytConsts.IDENTIFIER_NAME.equals(attachmentId.getSource())) {
      identifier = identifierController.findIdentifierById(attachmentId);
      if (identifier != null) {
        return new AttachmentId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(attachmentId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.ATTACHMENT, MikkeliNytConsts.IDENTIFIER_NAME, attachmentId.getId());
      if (identifier != null) {
        return new AttachmentId(MikkeliNytConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public NewsArticleId translate(NewsArticleId newsArticleId, String target) {
    return null;
  }

  @Override
  public BannerId translate(BannerId bannerId, String target) {
    return null;
  }
  
  @Override
  public TileId translate(TileId tileId, String target) {
    return null;
  }

  @Override
  public PageId translate(PageId pageId, String target) {
    return null;
  }

  @Override
  public MenuId translate(MenuId menuId, String target) {
    return null;
  }

  @Override
  public FileId translate(FileId fileId, String target) {
    return null;
  }

  @Override
  public MenuItemId translate(MenuItemId menuItemId, String target) {
    return null;
  }

}
