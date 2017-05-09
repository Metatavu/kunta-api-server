package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.index.ServiceLocationServiceChannelSearcher;
import fi.otavanopisto.kuntaapi.server.index.ServiceSearcher;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceProvider;
import fi.otavanopisto.kuntaapi.server.utils.ListUtils;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class ServiceController {

  @Inject
  private EntityController entityController;
  
  @Inject
  private ServiceSearcher serviceSearcher;
  
  @Inject
  private ServiceLocationServiceChannelSearcher serviceLocationServiceChannelSearcher;

  @Inject
  private Instance<ServiceProvider> serviceProviders;
  
  @Inject
  private Instance<ServiceChannelProvider> serviceChannelProviders;

  public Service findService(ServiceId serviceId) {
    for (ServiceProvider serviceProvider : getServiceProviders()) {
      Service service = serviceProvider.findService(serviceId);
      if (service != null) {
        return service;
      }
    }
    
    return null;
  }
  
  public List<Service> listServices(OrganizationId organizationId, Long firstResult, Long maxResults) {
    List<Service> result = new ArrayList<>();
    
    for (ServiceProvider serviceProvider : getServiceProviders()) {
      result.addAll(serviceProvider.listServices(organizationId));
    }
    
    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
  }

  public SearchResult<Service> searchServices(OrganizationId organizationId, String search, Long firstResult, Long maxResults) {
    SearchResult<ServiceId> searchResult = serviceSearcher.searchServices(organizationId, search, firstResult, maxResults);
    if (searchResult != null) {
      List<Service> result = new ArrayList<>(searchResult.getResult().size());
      
      for (ServiceId serviceId : searchResult.getResult()) {
        Service service = findService(serviceId);
        if (service != null) {
          result.add(service);
        }
      }
      
      return new SearchResult<>(result, searchResult.getTotalHits());
    }
    
    return SearchResult.emptyResult();
  }
  
  public ElectronicServiceChannel findElectronicServiceChannel(ElectronicServiceChannelId electronicChannelId) {
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      ElectronicServiceChannel electronicChannel = serviceChannelProvider.findElectronicServiceChannel(electronicChannelId);
      if (electronicChannel != null) {
        return electronicChannel;
      }
    }
    
    return null;
  }

  public PhoneServiceChannel findPhoneServiceChannel(PhoneServiceChannelId phoneChannelId) {
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      PhoneServiceChannel phoneChannel = serviceChannelProvider.findPhoneServiceChannel(phoneChannelId);
      if (phoneChannel != null) {
        return phoneChannel;
      }
    }
    
    return null;
  }

  public PrintableFormServiceChannel findPrintableFormServiceChannel(PrintableFormServiceChannelId printableFormChannelId) {
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      PrintableFormServiceChannel printableFormChannel = serviceChannelProvider.findPrintableFormServiceChannel(printableFormChannelId);
      if (printableFormChannel != null) {
        return printableFormChannel;
      }
    }
    
    return null;
  }

  public ServiceLocationServiceChannel findServiceLocationServiceChannel(ServiceLocationServiceChannelId serviceLocationChannelId) {
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      ServiceLocationServiceChannel serviceLocationChannel = serviceChannelProvider.findServiceLocationServiceChannel(serviceLocationChannelId);
      if (serviceLocationChannel != null) {
        return serviceLocationChannel;
      }
    }
    
    return null;
  }

  public WebPageServiceChannel findWebPageServiceChannel(WebPageServiceChannelId webPageChannelId) {
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      WebPageServiceChannel webPageChannel = serviceChannelProvider.findWebPageServiceChannelChannel(webPageChannelId);
      if (webPageChannel != null) {
        return webPageChannel;
      }
    }
    
    return null;
  }

  public List<ElectronicServiceChannel> listElectronicServiceChannels(Long firstResult, Long maxResults) {
    List<ElectronicServiceChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listElectronicServiceChannels());
    }

    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
  }

  public List<PhoneServiceChannel> listPhoneServiceChannels(Long firstResult, Long maxResults) {
    List<PhoneServiceChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listPhoneServiceChannels());
    }

    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
  }

  public List<PrintableFormServiceChannel> listPrintableFormServiceChannels(Long firstResult, Long maxResults) {
    List<PrintableFormServiceChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listPrintableFormServiceChannels());
    }

    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
  }

  public List<ServiceLocationServiceChannel> listServiceLocationServiceChannels(Long firstResult, Long maxResults) {
    List<ServiceLocationServiceChannel> result = new ArrayList<>();
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listServiceLocationServiceChannels());
    }

    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
  }
  
  public SearchResult<ServiceLocationServiceChannel> searchServiceLocationServiceChannels(OrganizationId kuntaApiOrganizationId, String search, Long firstResult, Long maxResults) {
    SearchResult<ServiceLocationServiceChannelId> searchResult = serviceLocationServiceChannelSearcher.searchServiceLocationServiceChannels(kuntaApiOrganizationId, search, firstResult, maxResults);
    if (searchResult == null) {
      return SearchResult.emptyResult();
    }
    
    List<ServiceLocationServiceChannel> result = new ArrayList<>(searchResult.getResult().size());
    for (ServiceLocationServiceChannelId serviceLocationServiceChannelId : searchResult.getResult()) {
      ServiceLocationServiceChannel serviceLocationServiceChannel = findServiceLocationServiceChannel(serviceLocationServiceChannelId);
      if (serviceLocationServiceChannel != null) {
        result.add(serviceLocationServiceChannel);
      }
    }
    
    return new SearchResult<>(result, searchResult.getTotalHits());
  }   

  public List<WebPageServiceChannel> listWebPageServiceChannels(Long firstResult, Long maxResults) {
    List<WebPageServiceChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listWebPageServiceChannelsChannels());
    }

    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
  }
  
  private List<ServiceProvider> getServiceProviders() {
    List<ServiceProvider> result = new ArrayList<>();
    
    Iterator<ServiceProvider> iterator = serviceProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
  private List<ServiceChannelProvider> getServiceChannelProviders() {
    List<ServiceChannelProvider> result = new ArrayList<>();
    
    Iterator<ServiceChannelProvider> iterator = serviceChannelProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
}
