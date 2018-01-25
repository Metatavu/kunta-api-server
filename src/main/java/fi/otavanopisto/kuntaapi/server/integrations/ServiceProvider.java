package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;

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
  
  /**
   * Updates service
   * 
   * @param serviceId service id
   * @param service new data for service
   * @return updated service
   */
  public IntegrationResponse<Service> updateService(ServiceId serviceId, Service service);
}
