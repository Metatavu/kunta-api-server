package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PhoneChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageChannelId;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicChannel;
import fi.metatavu.kuntaapi.server.rest.model.PhoneChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageChannel;

/**
 * Provider for service channels
 * 
 * @author Antti Leppä
 */
public interface ServiceChannelProvider {

  /**
   * Finds service electronic channel
   * 
   * @param serviceId serviceId
   * @param electronicServiceChannelId electronic service channel id
   * @return Returns a service electronic channel
   */
  public ElectronicChannel findElectronicChannel(ServiceId serviceId, ElectronicServiceChannelId electronicServiceChannelId);
  
  /**
   * Finds service phone channel
   * 
   * @param serviceId serviceId
   * @param phoneChannelId phone service channel id
   * @return Returns a service phone channel
   */
  public PhoneChannel findPhoneChannel(ServiceId serviceId, PhoneChannelId phoneChannelId);
  
  /**
   * Finds service printable form channel
   * 
   * @param serviceId serviceId
   * @param printableFormChannelId printable form channel id
   * @return Returns a service printable form channel
   */
  public PrintableFormChannel findPrintableFormChannel(ServiceId serviceId, PrintableFormChannelId printableFormChannelId);
  
  /**
   * Finds service location channel
   * 
   * @param serviceId serviceId
   * @param serviceLocationChannelId service location channel id
   * @return Returns a service location channel
   */
  public ServiceLocationChannel findServiceLocationChannel(ServiceId serviceId, ServiceLocationChannelId serviceLocationChannelId);
  
  /**
   * Finds service webpage channel
   * 
   * @param serviceId serviceId
   * @param webPageChannelId web page channel id
   * @return Returns a service webpage channel
   */
  public WebPageChannel findWebPageChannelChannel(ServiceId serviceId, WebPageChannelId webPageChannelId);

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
