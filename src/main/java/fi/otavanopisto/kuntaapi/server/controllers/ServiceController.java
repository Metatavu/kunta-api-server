package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.index.ServiceSearcher;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Service;

@Dependent
public class ServiceController {
  
  @Inject
  private ServiceSearcher serviceSearcher;

  @Inject
  private Instance<ServiceProvider> serviceProviders;

  public Service findService(ServiceId serviceId) {
    for (ServiceProvider serviceProvider : getServiceProviders()) {
      Service service = serviceProvider.findService(serviceId);
      if (service != null) {
        return service;
      }
    }
    
    return null;
  }
  
  public List<Service> listServices(Long firstResult, Long maxResults) {
    List<Service> result = new ArrayList<>();
    
    for (ServiceProvider serviceProvider : getServiceProviders()) {
      result.addAll(serviceProvider.listServices(null, null));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return result.subList(firstIndex, toIndex);
  }

  public List<Service> searchServices(String search, Long firstResult, Long maxResults) {
    SearchResult<ServiceId> searchResult = serviceSearcher.searchServices(search, firstResult, maxResults);
    if (searchResult != null) {
      List<Service> result = new ArrayList<>(searchResult.getResult().size());
      
      for (ServiceId serviceId : searchResult.getResult()) {
        Service service = findService(serviceId);
        if (service != null) {
          result.add(service);
        }
      }
      
      return result;
    }
    
    return Collections.emptyList();
  }
  
  private List<ServiceProvider> getServiceProviders() {
    List<ServiceProvider> result = new ArrayList<>();
    
    Iterator<ServiceProvider> iterator = serviceProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
}
