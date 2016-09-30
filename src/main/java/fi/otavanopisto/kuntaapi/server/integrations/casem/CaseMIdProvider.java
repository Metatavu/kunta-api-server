package fi.otavanopisto.kuntaapi.server.integrations.casem;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentId;
import fi.otavanopisto.kuntaapi.server.integrations.BannerId;
import fi.otavanopisto.kuntaapi.server.integrations.EventId;
import fi.otavanopisto.kuntaapi.server.integrations.FileId;
import fi.otavanopisto.kuntaapi.server.integrations.IdProvider;
import fi.otavanopisto.kuntaapi.server.integrations.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.MenuId;
import fi.otavanopisto.kuntaapi.server.integrations.MenuItemId;
import fi.otavanopisto.kuntaapi.server.integrations.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceClassId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.TileId;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

/**
 * Id provider for Case M
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@Dependent
public class CaseMIdProvider implements IdProvider {
  
  @Inject
  private IdentifierController identifierController;

  private CaseMIdProvider() {
  }
  
  @Override
  public boolean canTranslate(String source, String target) {
    if (CaseMConsts.IDENTIFIER_NAME.equals(source) && KuntaApiConsts.IDENTIFIER_NAME.equals(target)) {
      return true;
    }
    
    if (CaseMConsts.IDENTIFIER_NAME.equals(target) && KuntaApiConsts.IDENTIFIER_NAME.equals(source)) {
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
  public ServiceChannelId translate(ServiceChannelId serviceChannelId, String target) {
    return null;
  }

  @Override
  public ServiceClassId translate(ServiceClassId serviceClassId, String target) {
    return null;
  }

  @Override
  public EventId translate(EventId eventId, String target) {
    return null;
  }

  @Override
  public NewsArticleId translate(NewsArticleId newsArticleId, String target) {
    return null;
  }

  @Override
  public AttachmentId translate(AttachmentId attachmentId, String target) {
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
    Identifier identifier;
    
    if (CaseMConsts.IDENTIFIER_NAME.equals(pageId.getSource())) {
      identifier = identifierController.findIdentifierById(pageId);
      if (identifier != null) {
        return new PageId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(pageId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.PAGE, CaseMConsts.IDENTIFIER_NAME, pageId.getId());
      if (identifier != null) {
        return new PageId(CaseMConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public MenuId translate(MenuId menuId, String target) {
    return null;
  }

  @Override
  public FileId translate(FileId fileId, String target) {
    Identifier identifier;
    
    if (CaseMConsts.IDENTIFIER_NAME.equals(fileId.getSource())) {
      identifier = identifierController.findIdentifierById(fileId);
      if (identifier != null) {
        return new FileId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(fileId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.FILE, CaseMConsts.IDENTIFIER_NAME, fileId.getId());
      if (identifier != null) {
        return new FileId(CaseMConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public MenuItemId translate(MenuItemId menuItemId, String target) {
    return null;
  }

}
