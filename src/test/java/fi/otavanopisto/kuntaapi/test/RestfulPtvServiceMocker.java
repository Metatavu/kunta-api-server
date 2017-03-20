package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.metatavu.restfulptv.client.model.ElectronicChannel;
import fi.metatavu.restfulptv.client.model.PhoneChannel;
import fi.metatavu.restfulptv.client.model.PrintableFormChannel;
import fi.metatavu.restfulptv.client.model.Service;
import fi.metatavu.restfulptv.client.model.ServiceLocationChannel;
import fi.metatavu.restfulptv.client.model.WebPageChannel;

@SuppressWarnings ("squid:S1166")
public class RestfulPtvServiceMocker extends AbstractBaseMocker {

  private static final String SERVICES_PATH = String.format("%s/services", AbstractIntegrationTest.BASE_URL);
  private static final String ELECTRONIC_CHANNELS_PATH = String.format("%s/%%s/electronicChannels", SERVICES_PATH);
  private static final String PHONE_CHANNELS_PATH = String.format("%s/%%s/phoneChannels", SERVICES_PATH);
  private static final String PRINTABLE_FORM_CHANNELS_PATH = String.format("%s/%%s/printableFormChannels", SERVICES_PATH);
  private static final String LOCATION_CHANNELS_PATH = String.format("%s/%%s/serviceLocationChannels", SERVICES_PATH);
  private static final String WEBPAGE_CHANNELS_PATH = String.format("%s/%%s/webPageChannels", SERVICES_PATH);
  private static final String CHANNEL_TEMPLATE = "%s/%s";
  
  private ResourceMocker<String, Service> serviceMocker = new ResourceMocker<>();

  public RestfulPtvServiceMocker() {
    serviceMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(SERVICES_PATH));
  }
  
  @Override
  public void startMock() {
    super.startMock();
    serviceMocker.start();
  }
  
  @Override
  public void endMock() {
    serviceMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks resources
   * 
   * @param ids resource ids
   * @return mocker mocker
   */
  public RestfulPtvServiceMocker mockServices(String... ids) {
    try {
      for (String id : ids) {
        if (!serviceMocker.isMocked(id)) {
          mockService(readServiceFromJSONFile(String.format("services/%s.json", id)));
          
          ResourceMocker<String, ElectronicChannel> electronicChannelMocker = new ResourceMocker<>();
          electronicChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(String.format(ELECTRONIC_CHANNELS_PATH, id)));
          serviceMocker.addSubMocker(id, electronicChannelMocker);
          
          ResourceMocker<String, PhoneChannel> phoneChannelMocker = new ResourceMocker<>();
          phoneChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(String.format(PHONE_CHANNELS_PATH, id)));
          serviceMocker.addSubMocker(id, phoneChannelMocker);

          ResourceMocker<String, PrintableFormChannel> printableFormChannelMocker = new ResourceMocker<>();
          printableFormChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(String.format(PRINTABLE_FORM_CHANNELS_PATH, id)));
          serviceMocker.addSubMocker(id, printableFormChannelMocker);

          ResourceMocker<String, PrintableFormChannel> locationChannelMocker = new ResourceMocker<>();
          locationChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(String.format(LOCATION_CHANNELS_PATH, id)));
          serviceMocker.addSubMocker(id, locationChannelMocker);

          ResourceMocker<String, WebPageChannel> webPageChannelMocker = new ResourceMocker<>();
          webPageChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(String.format(WEBPAGE_CHANNELS_PATH, id)));
          serviceMocker.addSubMocker(id, webPageChannelMocker);
        } else {
          serviceMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }

    return this;
  }
  
  /**
   * Mocks electronic service
   * 
   * @param ids page ids
   * @return mocker
   */
  public RestfulPtvServiceMocker mockElectronicServiceChannels(String serviceId, String... ids) {
    for (String id : ids) {
      @SuppressWarnings("unchecked")
      ResourceMocker<String, ElectronicChannel> channelMocker = (ResourceMocker<String, ElectronicChannel>) serviceMocker.getSubMocker(serviceId, 0);
      if (!channelMocker.isMocked(id)) {
        channelMocker.add(id, readElectronicChannelFromJSONFile(String.format("electronicservicechannels/%s.json", id)), urlPathEqualTo(String.format(CHANNEL_TEMPLATE, String.format(ELECTRONIC_CHANNELS_PATH, serviceId), id)));
      } else {
        channelMocker.setStatus(id, MockedResourceStatus.OK);
      }
    }
  
    return this;
  }
  
  /**
   * Mocks phone service
   * 
   * @param ids page ids
   * @return mocker
   */
  public RestfulPtvServiceMocker mockPhoneServiceChannels(String serviceId, String... ids) {
    for (String id : ids) {
      @SuppressWarnings("unchecked")
      ResourceMocker<String, PhoneChannel> channelMocker = (ResourceMocker<String, PhoneChannel>) serviceMocker.getSubMocker(serviceId, 1);
      if (!channelMocker.isMocked(id)) {
        channelMocker.add(id, readPhoneChannelFromJSONFile(String.format("phonechannels/%s.json", id)), urlPathEqualTo(String.format(CHANNEL_TEMPLATE, String.format(PHONE_CHANNELS_PATH, serviceId), id)));
      } else {
        channelMocker.setStatus(id, MockedResourceStatus.OK);
      }
    }
  
    return this;
  }
  
  /**
   * Mocks printableForm service
   * 
   * @param ids page ids
   * @return mocker
   */
  public RestfulPtvServiceMocker mockPrintableFormServiceChannels(String serviceId, String... ids) {
    for (String id : ids) {
      @SuppressWarnings("unchecked")
      ResourceMocker<String, PrintableFormChannel> channelMocker = (ResourceMocker<String, PrintableFormChannel>) serviceMocker.getSubMocker(serviceId, 2);
      if (!channelMocker.isMocked(id)) {
        channelMocker.add(id, readPrintableFormChannelFromJSONFile(String.format("printableformchannels/%s.json", id)), urlPathEqualTo(String.format(CHANNEL_TEMPLATE, String.format(PRINTABLE_FORM_CHANNELS_PATH, serviceId), id)));
      } else {
        channelMocker.setStatus(id, MockedResourceStatus.OK);
      }
    }
  
    return this;
  }
  
  /**
   * Mocks location service
   * 
   * @param ids page ids
   * @return mocker
   */
  public RestfulPtvServiceMocker mockServiceLocationServiceChannels(String serviceId, String... ids) {
    for (String id : ids) {
      @SuppressWarnings("unchecked")
      ResourceMocker<String, ServiceLocationChannel> channelMocker = (ResourceMocker<String, ServiceLocationChannel>) serviceMocker.getSubMocker(serviceId, 3);
      if (!channelMocker.isMocked(id)) {
        channelMocker.add(id, readServiceLocationChannelFromJSONFile(String.format("servicelocationchannels/%s.json", id)), urlPathEqualTo(String.format(CHANNEL_TEMPLATE, String.format(LOCATION_CHANNELS_PATH, serviceId), id)));
      } else {
        channelMocker.setStatus(id, MockedResourceStatus.OK);
      }
    }
  
    return this;
  }
  
  /**
   * Mocks webPage service
   * 
   * @param ids page ids
   * @return mocker
   */
  public RestfulPtvServiceMocker mockWebPageServiceChannels(String serviceId, String... ids) {
    for (String id : ids) {
      @SuppressWarnings("unchecked")
      ResourceMocker<String, WebPageChannel> channelMocker = (ResourceMocker<String, WebPageChannel>) serviceMocker.getSubMocker(serviceId, 4);
      if (!channelMocker.isMocked(id)) {
        channelMocker.add(id, readWebPageChannelFromJSONFile(String.format("webpagechannels/%s.json", id)), urlPathEqualTo(String.format(CHANNEL_TEMPLATE, String.format(WEBPAGE_CHANNELS_PATH, serviceId), id)));
      } else {
        channelMocker.setStatus(id, MockedResourceStatus.OK);
      }
    }
  
    return this;
  }
  
  /**
   * Unmocks resources
   * 
   * @param ids resource ids
   * @return mocker mocker
   */
  public RestfulPtvServiceMocker unmockServices(String... ids) {
    for (String id : ids) {
      serviceMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  private void mockService(Service resource) throws JsonProcessingException {
    String id = resource.getId();
    String path = String.format(CHANNEL_TEMPLATE, SERVICES_PATH, id);
    serviceMocker.add(id, resource, urlPathEqualTo(path));
  }
  
  /**
   * Reads JSON file as organization object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Service readServiceFromJSONFile(String file) {
    return readJSONFile(file, Service.class);
  }
  
  /**
   * Reads JSON file as electronic service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private ElectronicChannel readElectronicChannelFromJSONFile(String file) {
    return readJSONFile(file, ElectronicChannel.class);
  }
  
  /**
   * Reads JSON file as phone service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private PhoneChannel readPhoneChannelFromJSONFile(String file) {
    return readJSONFile(file, PhoneChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private PrintableFormChannel readPrintableFormChannelFromJSONFile(String file) {
    return readJSONFile(file, PrintableFormChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private ServiceLocationChannel readServiceLocationChannelFromJSONFile(String file) {
    return readJSONFile(file, ServiceLocationChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private WebPageChannel readWebPageChannelFromJSONFile(String file) {
    return readJSONFile(file, WebPageChannel.class);
  }
  
}
