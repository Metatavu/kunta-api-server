package fi.otavanopisto.kuntaapi.server.integrations.mwp;

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
 * Id provider for management Wordpress service
 * 
 * @author Antti Leppä
 */
@Dependent
public class MwpIdProvider implements IdProvider {
  
  @Inject
  private IdentifierController identifierController;

  private MwpIdProvider() {
  }
  
  @Override
  public boolean canTranslate(String source, String target) {
    if (MwpConsts.IDENTIFIER_NAME.equals(source) && KuntaApiConsts.IDENTIFIER_NAME.equals(target)) {
      return true;
    }
    
    if (MwpConsts.IDENTIFIER_NAME.equals(target) && KuntaApiConsts.IDENTIFIER_NAME.equals(source)) {
      return true;
    }
    
    return false;
  }

  @Override
  public OrganizationId translate(OrganizationId organizationId, String target) {
    Identifier identifier;
    
    if (MwpConsts.IDENTIFIER_NAME.equals(organizationId.getSource())) {
      identifier = identifierController.findIdentifierById(organizationId);
      if (identifier != null) {
        return new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      } 
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(organizationId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.ORGANIZATION, MwpConsts.IDENTIFIER_NAME, organizationId.getId());
      if (identifier != null) {
        return new OrganizationId(MwpConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public ServiceId translate(ServiceId serviceId, String target) {
    Identifier identifier;
    
    if (MwpConsts.IDENTIFIER_NAME.equals(serviceId.getSource())) {
      identifier = identifierController.findIdentifierById(serviceId);
      if (identifier != null) {
        return new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(serviceId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.SERVICE, MwpConsts.IDENTIFIER_NAME, serviceId.getId());
      if (identifier != null) {
        return new ServiceId(MwpConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    
    return null;
  }
  
  @Override
  public ServiceChannelId translate(ServiceChannelId serviceChannelId, String target) {
    return null;
  }

  @Override
  public ServiceClassId translate(ServiceClassId serviceClassId, String target) {
    Identifier identifier;
    
    if (MwpConsts.IDENTIFIER_NAME.equals(serviceClassId.getSource())) {
      identifier = identifierController.findIdentifierById(serviceClassId);
      if (identifier != null) {
        return new ServiceClassId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(serviceClassId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.SERVICE_CLASS, MwpConsts.IDENTIFIER_NAME, serviceClassId.getId());
      if (identifier != null) {
        return new ServiceClassId(MwpConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public EventId translate(EventId eventId, String target) {
    return null;
  }

  @Override
  public NewsArticleId translate(NewsArticleId newsArticleId, String target) {
    Identifier identifier;
    
    if (MwpConsts.IDENTIFIER_NAME.equals(newsArticleId.getSource())) {
      identifier = identifierController.findIdentifierById(newsArticleId);
      if (identifier != null) {
        return new NewsArticleId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(newsArticleId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.NEWS_ARTICLE, MwpConsts.IDENTIFIER_NAME, newsArticleId.getId());
      if (identifier != null) {
        return new NewsArticleId(MwpConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public AttachmentId translate(AttachmentId attachmentId, String target) {
    Identifier identifier;
    
    if (MwpConsts.IDENTIFIER_NAME.equals(attachmentId.getSource())) {
      identifier = identifierController.findIdentifierById(attachmentId);
      if (identifier != null) {
        return new AttachmentId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(attachmentId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.ATTACHMENT, MwpConsts.IDENTIFIER_NAME, attachmentId.getId());
      if (identifier != null) {
        return new AttachmentId(MwpConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public BannerId translate(BannerId bannerId, String target) {
    Identifier identifier;
    
    if (MwpConsts.IDENTIFIER_NAME.equals(bannerId.getSource())) {
      identifier = identifierController.findIdentifierById(bannerId);
      if (identifier != null) {
        return new BannerId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(bannerId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.BANNER, MwpConsts.IDENTIFIER_NAME, bannerId.getId());
      if (identifier != null) {
        return new BannerId(MwpConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public TileId translate(TileId tileId, String target) {
    Identifier identifier;
    
    if (MwpConsts.IDENTIFIER_NAME.equals(tileId.getSource())) {
      identifier = identifierController.findIdentifierById(tileId);
      if (identifier != null) {
        return new TileId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(tileId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.TILE, MwpConsts.IDENTIFIER_NAME, tileId.getId());
      if (identifier != null) {
        return new TileId(MwpConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public PageId translate(PageId pageId, String target) {
    Identifier identifier;
    
    if (MwpConsts.IDENTIFIER_NAME.equals(pageId.getSource())) {
      identifier = identifierController.findIdentifierById(pageId);
      if (identifier != null) {
        return new PageId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(pageId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.PAGE, MwpConsts.IDENTIFIER_NAME, pageId.getId());
      if (identifier != null) {
        return new PageId(MwpConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public MenuId translate(MenuId menuId, String target) {
    Identifier identifier;
    
    if (MwpConsts.IDENTIFIER_NAME.equals(menuId.getSource())) {
      identifier = identifierController.findIdentifierById(menuId);
      if (identifier != null) {
        return new MenuId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(menuId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.MENU, MwpConsts.IDENTIFIER_NAME, menuId.getId());
      if (identifier != null) {
        return new MenuId(MwpConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public FileId translate(FileId fileId, String target) {
    Identifier identifier;
    
    if (MwpConsts.IDENTIFIER_NAME.equals(fileId.getSource())) {
      identifier = identifierController.findIdentifierById(fileId);
      if (identifier != null) {
        return new FileId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(fileId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.FILE, MwpConsts.IDENTIFIER_NAME, fileId.getId());
      if (identifier != null) {
        return new FileId(MwpConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public MenuItemId translate(MenuItemId menuItemId, String target) {
    Identifier identifier;
    
    if (MwpConsts.IDENTIFIER_NAME.equals(menuItemId.getSource())) {
      identifier = identifierController.findIdentifierById(menuItemId);
      if (identifier != null) {
        return new MenuItemId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(menuItemId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.MENU_ITEM, MwpConsts.IDENTIFIER_NAME, menuItemId.getId());
      if (identifier != null) {
        return new MenuItemId(MwpConsts.IDENTIFIER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

}
