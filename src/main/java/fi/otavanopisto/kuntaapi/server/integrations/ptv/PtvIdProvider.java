package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.IdProvider;
import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.PhoneChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationChannelId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.id.WebPageChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.FileId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

/**
 * Id provider for palvelu tieto varanto
 * 
 * @author Otavan Opisto
 */
@Dependent
public class PtvIdProvider implements IdProvider {
  
  @Inject
  private IdentifierController identifierController;

  private PtvIdProvider() {
  }
  
  @Override
  public boolean canTranslate(String source, String target) {
    if (PtvConsts.IDENTIFIFER_NAME.equals(source) && KuntaApiConsts.IDENTIFIER_NAME.equals(target)) {
      return true;
    }
    
    if (PtvConsts.IDENTIFIFER_NAME.equals(target) && KuntaApiConsts.IDENTIFIER_NAME.equals(source)) {
      return true;
    }
    
    return false;
  }

  @Override
  public OrganizationId translate(OrganizationId organizationId, String target) {
    Identifier identifier;
    
    if (PtvConsts.IDENTIFIFER_NAME.equals(organizationId.getSource())) {
      identifier = identifierController.findIdentifierById(organizationId);
      if (identifier != null) {
        return new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(organizationId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.ORGANIZATION, PtvConsts.IDENTIFIFER_NAME, organizationId.getId());
      if (identifier != null) {
        return new OrganizationId(PtvConsts.IDENTIFIFER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public ServiceId translate(ServiceId serviceId, String target) {
    Identifier identifier;
    
    if (PtvConsts.IDENTIFIFER_NAME.equals(serviceId.getSource())) {
      identifier = identifierController.findIdentifierById(serviceId);
      if (identifier != null) {
        return new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      } 
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(serviceId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.SERVICE, PtvConsts.IDENTIFIFER_NAME, serviceId.getId());
      if (identifier != null) {
        return new ServiceId(PtvConsts.IDENTIFIFER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public ElectronicServiceChannelId translate(ElectronicServiceChannelId serviceChannelId, String target) {
    Identifier identifier;
    
    if (PtvConsts.IDENTIFIFER_NAME.equals(serviceChannelId.getSource())) {
      identifier = identifierController.findIdentifierById(serviceChannelId);
      if (identifier != null) {
        return new ElectronicServiceChannelId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(serviceChannelId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.ELECTRONIC_SERVICE_CHANNEL, PtvConsts.IDENTIFIFER_NAME, serviceChannelId.getId());
      if (identifier != null) {
        return new ElectronicServiceChannelId(PtvConsts.IDENTIFIFER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }
  
  @Override
  public PhoneChannelId translate(PhoneChannelId serviceChannelId, String target) {
    Identifier identifier;
    
    if (PtvConsts.IDENTIFIFER_NAME.equals(serviceChannelId.getSource())) {
      identifier = identifierController.findIdentifierById(serviceChannelId);
      if (identifier != null) {
        return new PhoneChannelId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(serviceChannelId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.PHONE_CHANNEL, PtvConsts.IDENTIFIFER_NAME, serviceChannelId.getId());
      if (identifier != null) {
        return new PhoneChannelId(PtvConsts.IDENTIFIFER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }
  
  @Override
  public PrintableFormChannelId translate(PrintableFormChannelId serviceChannelId, String target) {
    Identifier identifier;
    
    if (PtvConsts.IDENTIFIFER_NAME.equals(serviceChannelId.getSource())) {
      identifier = identifierController.findIdentifierById(serviceChannelId);
      if (identifier != null) {
        return new PrintableFormChannelId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(serviceChannelId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.PRINTABLE_FORM_CHANNEL, PtvConsts.IDENTIFIFER_NAME, serviceChannelId.getId());
      if (identifier != null) {
        return new PrintableFormChannelId(PtvConsts.IDENTIFIFER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }
  
  @Override
  public ServiceLocationChannelId translate(ServiceLocationChannelId serviceChannelId, String target) {
    Identifier identifier;
    
    if (PtvConsts.IDENTIFIFER_NAME.equals(serviceChannelId.getSource())) {
      identifier = identifierController.findIdentifierById(serviceChannelId);
      if (identifier != null) {
        return new ServiceLocationChannelId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(serviceChannelId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.SERVICE_LOCATION_CHANNEL, PtvConsts.IDENTIFIFER_NAME, serviceChannelId.getId());
      if (identifier != null) {
        return new ServiceLocationChannelId(PtvConsts.IDENTIFIFER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }
  
  @Override
  public WebPageChannelId translate(WebPageChannelId serviceChannelId, String target) {
    Identifier identifier;
    
    if (PtvConsts.IDENTIFIFER_NAME.equals(serviceChannelId.getSource())) {
      identifier = identifierController.findIdentifierById(serviceChannelId);
      if (identifier != null) {
        return new WebPageChannelId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(serviceChannelId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.WEBPAGE_CHANNEL, PtvConsts.IDENTIFIFER_NAME, serviceChannelId.getId());
      if (identifier != null) {
        return new WebPageChannelId(PtvConsts.IDENTIFIFER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }
  
  @Override
  public OrganizationServiceId translate(OrganizationServiceId organizationServiceId, String target) {
    Identifier identifier;
    
    if (PtvConsts.IDENTIFIFER_NAME.equals(organizationServiceId.getSource())) {
      identifier = identifierController.findIdentifierById(organizationServiceId);
      if (identifier != null) {
        return new OrganizationServiceId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      }
    } else if (KuntaApiConsts.IDENTIFIER_NAME.equals(organizationServiceId.getSource())) {
      identifier = identifierController.findIdentifierByTypeSourceAndKuntaApiId(IdType.ORGANIZATION_SERVICE, PtvConsts.IDENTIFIFER_NAME, organizationServiceId.getId());
      if (identifier != null) {
        return new OrganizationServiceId(PtvConsts.IDENTIFIFER_NAME, identifier.getSourceId());
      }
    }
    
    return null;
  }

  @Override
  public EventId translate(EventId eventId, String target) {
    return null;
  }

  @Override
  public AttachmentId translate(AttachmentId attachmentId, String target) {
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

  @Override
  public JobId translate(JobId jobId, String target) {
    return null;
  }

}
