package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvElectronicServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvPhoneServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvPrintableFormServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvServiceLocationServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvWebPageServiceChannelResourceContainer;
import javax.enterprise.context.ApplicationScoped;

/**
 * Service channel provider for PTV
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class PtvServiceChannelProvider implements ServiceChannelProvider {
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private PtvElectronicServiceChannelResourceContainer ptvElectronicServiceChannelResourceContainer;
  
  @Inject
  private PtvPhoneServiceChannelResourceContainer ptvPhoneServiceChannelResourceContainer;
  
  @Inject
  private PtvPrintableFormServiceChannelResourceContainer ptvPrintableFormServiceChannelResourceContainer;
  
  @Inject
  private PtvServiceLocationServiceChannelResourceContainer ptvServiceLocationServiceChannelResourceContainer;
  
  @Inject
  private PtvWebPageServiceChannelResourceContainer ptvWebPageServiceChannelResourceContainer;
  
  @Override
  public ElectronicServiceChannel findElectronicServiceChannel(ElectronicServiceChannelId electronicServiceChannelId) {
    return ptvElectronicServiceChannelResourceContainer.get(electronicServiceChannelId);
  }
  
  @Override
  public PhoneServiceChannel findPhoneServiceChannel(PhoneServiceChannelId phoneServiceChannelId) {
    return ptvPhoneServiceChannelResourceContainer.get(phoneServiceChannelId);
  }
  
  @Override
  public PrintableFormServiceChannel findPrintableFormServiceChannel(PrintableFormServiceChannelId printableFormServiceChannelId) {
    return ptvPrintableFormServiceChannelResourceContainer.get(printableFormServiceChannelId);
  }
  
  @Override
  public ServiceLocationServiceChannel findServiceLocationServiceChannel(ServiceLocationServiceChannelId serviceLocationChannelId) {
    return ptvServiceLocationServiceChannelResourceContainer.get(serviceLocationChannelId);
  }
  
  @Override
  public WebPageServiceChannel findWebPageServiceChannelChannel(WebPageServiceChannelId webPageServiceChannelId) {
    return ptvWebPageServiceChannelResourceContainer.get(webPageServiceChannelId);
  }
  
  @Override
  public List<ElectronicServiceChannel> listElectronicServiceChannels() {
    List<ElectronicServiceChannelId> electronicServiceChannelIds = identifierController.listElectronicServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, null, null);
    List<ElectronicServiceChannel> result = new ArrayList<>(electronicServiceChannelIds.size());
    
    for (ElectronicServiceChannelId electronicServiceChannelId : electronicServiceChannelIds) {
      ElectronicServiceChannel electronicServiceChannel = ptvElectronicServiceChannelResourceContainer.get(electronicServiceChannelId);
      if (electronicServiceChannel != null) {
        result.add(electronicServiceChannel);
      }
    }
    
    return result;
  }

  @Override
  public List<PhoneServiceChannel> listPhoneServiceChannels() {
    List<PhoneServiceChannelId> electronicServiceChannelIds = identifierController.listPhoneServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, null, null);
    List<PhoneServiceChannel> result = new ArrayList<>(electronicServiceChannelIds.size());
    
    for (PhoneServiceChannelId electronicServiceChannelId : electronicServiceChannelIds) {
      PhoneServiceChannel electronicServiceChannel = ptvPhoneServiceChannelResourceContainer.get(electronicServiceChannelId);
      if (electronicServiceChannel != null) {
        result.add(electronicServiceChannel);
      }
    }
    
    return result;
  }

  @Override
  public List<PrintableFormServiceChannel> listPrintableFormServiceChannels() {
    List<PrintableFormServiceChannelId> electronicServiceChannelIds = identifierController.listPrintableFormServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, null, null);
    List<PrintableFormServiceChannel> result = new ArrayList<>(electronicServiceChannelIds.size());
    
    for (PrintableFormServiceChannelId electronicServiceChannelId : electronicServiceChannelIds) {
      PrintableFormServiceChannel electronicServiceChannel = ptvPrintableFormServiceChannelResourceContainer.get(electronicServiceChannelId);
      if (electronicServiceChannel != null) {
        result.add(electronicServiceChannel);
      }
    }
    
    return result;
  }

  @Override
  public List<ServiceLocationServiceChannel> listServiceLocationServiceChannels() {
    List<ServiceLocationServiceChannelId> electronicServiceChannelIds = identifierController.listServiceLocationServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, null, null);
    List<ServiceLocationServiceChannel> result = new ArrayList<>(electronicServiceChannelIds.size());
    
    for (ServiceLocationServiceChannelId electronicServiceChannelId : electronicServiceChannelIds) {
      ServiceLocationServiceChannel electronicServiceChannel = ptvServiceLocationServiceChannelResourceContainer.get(electronicServiceChannelId);
      if (electronicServiceChannel != null) {
        result.add(electronicServiceChannel);
      }
    }
    
    return result;
  }

  @Override
  public List<WebPageServiceChannel> listWebPageServiceChannelsChannels() {
    List<WebPageServiceChannelId> electronicServiceChannelIds = identifierController.listWebPageServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, null, null);
    List<WebPageServiceChannel> result = new ArrayList<>(electronicServiceChannelIds.size());
    
    for (WebPageServiceChannelId electronicServiceChannelId : electronicServiceChannelIds) {
      WebPageServiceChannel electronicServiceChannel = ptvWebPageServiceChannelResourceContainer.get(electronicServiceChannelId);
      if (electronicServiceChannel != null) {
        result.add(electronicServiceChannel);
      }
    }
    
    return result;
  }
  
}
