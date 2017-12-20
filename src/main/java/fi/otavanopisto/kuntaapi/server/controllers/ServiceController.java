package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.index.search.ElectronicServiceChannelSearcher;
import fi.otavanopisto.kuntaapi.server.index.search.PhoneServiceChannelSearcher;
import fi.otavanopisto.kuntaapi.server.index.search.PrintableFormServiceChannelSearcher;
import fi.otavanopisto.kuntaapi.server.index.search.ServiceLocationServiceChannelSearcher;
import fi.otavanopisto.kuntaapi.server.index.search.ServiceSearcher;
import fi.otavanopisto.kuntaapi.server.index.search.WebPageServiceChannelSearcher;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.IntegrationResponse;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;
import fi.otavanopisto.kuntaapi.server.utils.ListUtils;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceOrganization;
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
  private ElectronicServiceChannelSearcher electronicServiceChannelSearcher;

  @Inject
  private PhoneServiceChannelSearcher phoneServiceChannelSearcher;
  
  @Inject
  private PrintableFormServiceChannelSearcher printableFormServiceChannelSearcher;
  
  @Inject
  private WebPageServiceChannelSearcher webPageServiceChannelSearcher;
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;

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

  /**
   * Updates service
   * 
   * @param serviceId service id
   * @param service new data for the service
   * @return updated service
   */
  public IntegrationResponse<Service> updateService(ServiceId serviceId, Service service) {
    for (ServiceProvider serviceProvider : getServiceProviders()) {
      IntegrationResponse<Service> updatedService = serviceProvider.updateService(serviceId, service);
      if (updatedService != null) {
        return updatedService;
      }
    }
    
    return null;
  }
  
  @SuppressWarnings ("squid:S00107")
  public SearchResult<Service> searchServices(OrganizationId organizationId, ElectronicServiceChannelId electronicServiceChannelId, PhoneServiceChannelId phoneServiceChannelId, PrintableFormServiceChannelId printableFormServiceChannelId, ServiceLocationServiceChannelId serviceLocationServiceChannelId, WebPageServiceChannelId webPageServiceChannelId, String search, ServiceSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    SearchResult<ServiceId> searchResult = serviceSearcher.searchServices(organizationId, 
        electronicServiceChannelId,
        phoneServiceChannelId,
        printableFormServiceChannelId,
        serviceLocationServiceChannelId,
        webPageServiceChannelId,
        search, 
        sortBy, 
        sortDir, 
        firstResult, 
        maxResults);
    
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
    
    return new SearchResult<>(Collections.emptyList(), 0);
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
  
  /**
   * Updates electronic service channel
   * 
   * @param electronicChannelId electronic service channel id
   * @param electronicServiceChannel new data for electronic service channel
   * @return updated electronic service channel
   */
  public IntegrationResponse<ElectronicServiceChannel> updateElectronicServiceChannel(ElectronicServiceChannelId electronicChannelId, ElectronicServiceChannel electronicServiceChannel) {
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      IntegrationResponse<ElectronicServiceChannel> updatedElectronicServiceChannel = serviceChannelProvider.updateElectronicServiceChannel(electronicChannelId, electronicServiceChannel);
      if (updatedElectronicServiceChannel != null) {
        return updatedElectronicServiceChannel;
      }
    }
    
    return null;
  }
  
  /**
   * Updates service location service channel
   * 
   * @param serviceLocationChannelId service location service channel id
   * @param serviceLocationServiceChannel new data for service location service channel
   * @return updated service location service channel
   */
  public IntegrationResponse<ServiceLocationServiceChannel> updateServiceLocationServiceChannel(ServiceLocationServiceChannelId serviceLocationChannelId, ServiceLocationServiceChannel serviceLocationServiceChannel) {
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      IntegrationResponse<ServiceLocationServiceChannel> updatedServiceLocationServiceChannel = serviceChannelProvider.updateServiceLocationServiceChannel(serviceLocationChannelId, serviceLocationServiceChannel);
      if (updatedServiceLocationServiceChannel != null) {
        return updatedServiceLocationServiceChannel;
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

  /**
   * Searches service location service Channels. All parameters can be nulled. Nulled parameters will be ignored
   * 
   * @param kuntaApiOrganizationId organization id
   * @param search free-text search
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param firstResult first result
   * @param maxResults max results
   * @return search result
   */
  public SearchResult<ServiceLocationServiceChannel> searchServiceLocationServiceChannels(OrganizationId kuntaApiOrganizationId, String search, ServiceChannelSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    SearchResult<ServiceLocationServiceChannelId> searchResult = serviceLocationServiceChannelSearcher.searchServiceChannels(kuntaApiOrganizationId, search, sortBy, sortDir, firstResult, maxResults);
    return processServiceChannelsResults(searchResult, this::findServiceLocationServiceChannel);
  }

  /**
   * Searches Electronic Service Channels. All parameters can be nulled. Nulled parameters will be ignored
   * 
   * @param kuntaApiOrganizationId organization id
   * @param search free-text search
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param firstResult first result
   * @param maxResults max results
   * @return search result
   */
  public SearchResult<ElectronicServiceChannel> searchElectronicServiceChannels(OrganizationId kuntaApiOrganizationId, String search, ServiceChannelSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    SearchResult<ElectronicServiceChannelId> searchResult = electronicServiceChannelSearcher.searchServiceChannels(kuntaApiOrganizationId, search, sortBy, sortDir, firstResult, maxResults);
    return processServiceChannelsResults(searchResult, this::findElectronicServiceChannel);
  }

  /**
   * Searches phone service Channels. All parameters can be nulled. Nulled parameters will be ignored
   * 
   * @param kuntaApiOrganizationId organization id
   * @param search free-text search
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param firstResult first result
   * @param maxResults max results
   * @return search result
   */
  public SearchResult<PhoneServiceChannel> searchPhoneServiceChannels(OrganizationId kuntaApiOrganizationId, String search, ServiceChannelSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    SearchResult<PhoneServiceChannelId> searchResult = phoneServiceChannelSearcher.searchServiceChannels(kuntaApiOrganizationId, search, sortBy, sortDir, firstResult, maxResults);
    return processServiceChannelsResults(searchResult, this::findPhoneServiceChannel);
  }

  /**
   * Searches printable form service Channels. All parameters can be nulled. Nulled parameters will be ignored
   * 
   * @param kuntaApiOrganizationId organization id
   * @param search free-text search
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param firstResult first result
   * @param maxResults max results
   * @return search result
   */
  public SearchResult<PrintableFormServiceChannel> searchPrintableFormServiceChannels(OrganizationId kuntaApiOrganizationId, String search, ServiceChannelSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    SearchResult<PrintableFormServiceChannelId> searchResult = printableFormServiceChannelSearcher.searchServiceChannels(kuntaApiOrganizationId, search, sortBy, sortDir, firstResult, maxResults);
    return processServiceChannelsResults(searchResult, this::findPrintableFormServiceChannel);
  }

  /**
   * Searches web page service Channels. All parameters can be nulled. Nulled parameters will be ignored
   * 
   * @param kuntaApiOrganizationId organization id
   * @param search free-text search
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param firstResult first result
   * @param maxResults max results
   * @return search result
   */
  public SearchResult<WebPageServiceChannel> searchWebPageServiceChannels(OrganizationId kuntaApiOrganizationId, String search, ServiceChannelSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    SearchResult<WebPageServiceChannelId> searchResult = webPageServiceChannelSearcher.searchServiceChannels(kuntaApiOrganizationId, search, sortBy, sortDir, firstResult, maxResults);
    return processServiceChannelsResults(searchResult, this::findWebPageServiceChannel);
  }

  public List<WebPageServiceChannel> listWebPageServiceChannels(Long firstResult, Long maxResults) {
    List<WebPageServiceChannel> result = new ArrayList<>();
    
    for (ServiceChannelProvider serviceChannelProvider : getServiceChannelProviders()) {
      result.addAll(serviceChannelProvider.listWebPageServiceChannelsChannels());
    }

    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
  }
  
  /**
   * Resolves service's main responsible organization. If not found null is returned
   * 
   * @param service service
   * @return service's main responsible organization or null if not found
   */
  public OrganizationId getServiceMainResponsibleOrganization(Service service) {
    List<ServiceOrganization> organizations = service.getOrganizations();
    for (ServiceOrganization organization : organizations) {
      if ("Responsible".equals(organization.getRoleType())) {
        return kuntaApiIdFactory.createOrganizationId(organization.getOrganizationId());
      }
    }
    
    return null;
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

  private <I extends BaseId, T> SearchResult<T> processServiceChannelsResults(SearchResult<I> searchResult, SearchResultFinder<I, T> resultFinder) {
    if (searchResult == null) {
      return SearchResult.emptyResult();
    }
    
    List<T> result = new ArrayList<>(searchResult.getResult().size());
    for (I serviceChannelId : searchResult.getResult()) {
      T electronicServiceChannel = resultFinder.find(serviceChannelId);
      if (electronicServiceChannel != null) {
        result.add(electronicServiceChannel);
      }
    }
    
    return new SearchResult<>(result, searchResult.getTotalHits());
  }
  
  @FunctionalInterface
  private interface SearchResultFinder<I extends BaseId, T> {
    
    T find(I id);
    
  }
}
