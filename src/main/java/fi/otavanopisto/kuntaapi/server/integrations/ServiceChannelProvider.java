package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.rest.model.ElectronicChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PhoneChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PrintableFormChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceLocationChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.WebPageChannel;

/**
 * Provider for service channels
 * 
 * @author Antti Leppä
 */
public interface ServiceChannelProvider {

  /**
   * Lists service electronic channels
   * 
   * @param organizationId organization id
   * @param serviceId serviceId
   * @return List of service electronic channels
   */
  public List<ElectronicChannel> listElectronicChannels(OrganizationId organizationId, ServiceId serviceId);
  
  /**
   * Lists service phone channels
   * 
   * @param organizationId organization id
   * @param serviceId serviceId
   * @return List of service phone channels
   */
  public List<PhoneChannel> listPhoneChannels(OrganizationId organizationId, ServiceId serviceId);
  
  /**
   * Lists service printable form channels
   * 
   * @param organizationId organization id
   * @param serviceId serviceId
   * @return List of service printable form channels
   */
  public List<PrintableFormChannel> listPrintableFormChannels(OrganizationId organizationId, ServiceId serviceId);
  
  /**
   * Lists service location channels
   * 
   * @param organizationId organization id
   * @param serviceId serviceId
   * @return List of service location channels
   */
  public List<ServiceLocationChannel> listServiceLocationChannels(OrganizationId organizationId, ServiceId serviceId);
  
  /**
   * Lists service webpage channels
   * 
   * @param organizationId organization id
   * @param serviceId serviceId
   * @return List of service webpage channels
   */
  public List<WebPageChannel> listWebPageChannelsChannels(OrganizationId organizationId, ServiceId serviceId);
  
}
