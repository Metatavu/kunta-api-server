package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;

/**
 * Provider for service channels
 * 
 * @author Antti Leppä
 */
public interface ServiceChannelProvider {

  /**
   * Finds electronic service channel
   * 
   * @param electronicServiceChannelId electronic service channel id
   * @return Returns a electronic service channel
   */
  public ElectronicServiceChannel findElectronicServiceChannel(ElectronicServiceChannelId electronicServiceChannelId);
  
  /**
   * Finds phone service channel
   * 
   * @param phoneChannelserviceId phone service channel id
   * @return Returns a phone service channel
   */
  public PhoneServiceChannel findPhoneServiceChannel(PhoneServiceChannelId phoneChannelserviceId);
  
  /**
   * Finds a printable form service channel
   * 
   * @param printableFormServiceChannelId printable form service channel id
   * @return Returns a printable form service channel
   */
  public PrintableFormServiceChannel findPrintableFormServiceChannel(PrintableFormServiceChannelId printableFormServiceChannelId);
  
  /**
   * Finds service location service channel
   * 
   * @param serviceLocationServiceChannelId service location service channel id
   * @return Returns a location service channel
   */
  public ServiceLocationServiceChannel findServiceLocationServiceChannel(ServiceLocationServiceChannelId serviceLocationServiceChannelId);
  
  /**
   * Finds service webpage channel
   * 
   * @param webPageServiceChannelId web page service channel id
   * @return Returns a webpage service channel
   */
  public WebPageServiceChannel findWebPageServiceChannelChannel(WebPageServiceChannelId webPageServiceChannelId);

  /**
   * Lists electronic service channels  
   * 
   * @return List of electronic service channels
   */
  public List<ElectronicServiceChannel> listElectronicServiceChannels();
  
  /**
   * Lists service phone channels
   * 
   * @param serviceId serviceId
   * @return List of service phone channels
   */
  public List<PhoneServiceChannel> listPhoneServiceChannels();
  
  /**
   * Lists printable form service channels
   * 
   * @return List of printable form service channels
   */
  public List<PrintableFormServiceChannel> listPrintableFormServiceChannels();
  
  /**
   * Lists location service channels
   * 
   * @return List of location service channels
   */
  public List<ServiceLocationServiceChannel> listServiceLocationServiceChannels();
  
  /**
   * Lists webpage service channels
   * 
   * @return List of webpage service channels
   */
  public List<WebPageServiceChannel> listWebPageServiceChannelsChannels();
  
}
