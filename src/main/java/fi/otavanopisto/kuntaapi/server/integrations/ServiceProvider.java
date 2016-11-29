package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.rest.model.Service;

/**
 * Interface that describes a single service provider
 * 
 * @author Antti Leppä
 */
public interface ServiceProvider {
  
  /**
   * Returns single service by serviceId or null if not found
   * 
   * @param serviceId service id
   * @return single service by serviceId or null if not found
   */
  public Service findService(ServiceId serviceId);
  
  /**
   * Lists services
   * 
   * @param organizationId filter results by organizationId
   * @return list of services
   */
  public List<Service> listServices(OrganizationId organizationId);
  
}
