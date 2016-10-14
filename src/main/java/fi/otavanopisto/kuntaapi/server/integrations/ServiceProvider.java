package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.rest.model.Service;

public interface ServiceProvider {
  
  public Service findService(ServiceId serviceId);
  
  public List<Service> listServices(Long firstResult, Long maxResults);
  
}
