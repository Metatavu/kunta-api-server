package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.ServiceId;
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
   * @param serviceId serviceId
   * @return List of service electronic channels
   */
  public List<ElectronicChannel> listElectronicChannels(ServiceId serviceId);
  
  /**
   * Lists service phone channels
   * 
   * @param serviceId serviceId
   * @return List of service phone channels
   */
  public List<PhoneChannel> listPhoneChannels(ServiceId serviceId);
  
  /**
   * Lists service printable form channels
   * 
   * @param serviceId serviceId
   * @return List of service printable form channels
   */
  public List<PrintableFormChannel> listPrintableFormChannels(ServiceId serviceId);
  
  /**
   * Lists service location channels
   * 
   * @param serviceId serviceId
   * @return List of service location channels
   */
  public List<ServiceLocationChannel> listServiceLocationChannels(ServiceId serviceId);
  
  /**
   * Lists service webpage channels
   * 
   * @param serviceId serviceId
   * @return List of service webpage channels
   */
  public List<WebPageChannel> listWebPageChannelsChannels(ServiceId serviceId);
  
}
