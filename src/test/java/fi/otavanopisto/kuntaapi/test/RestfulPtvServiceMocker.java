package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.metatavu.restfulptv.client.model.ElectronicServiceChannel;
import fi.metatavu.restfulptv.client.model.PhoneServiceChannel;
import fi.metatavu.restfulptv.client.model.PrintableFormServiceChannel;
import fi.metatavu.restfulptv.client.model.Service;
import fi.metatavu.restfulptv.client.model.ServiceLocationServiceChannel;
import fi.metatavu.restfulptv.client.model.WebPageServiceChannel;

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
          
          ResourceMocker<String, ElectronicServiceChannel> electronicChannelMocker = new ResourceMocker<>();
          electronicChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(String.format(ELECTRONIC_CHANNELS_PATH, id)));
          serviceMocker.addSubMocker(id, electronicChannelMocker);
          
          ResourceMocker<String, PhoneServiceChannel> phoneChannelMocker = new ResourceMocker<>();
          phoneChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(String.format(PHONE_CHANNELS_PATH, id)));
          serviceMocker.addSubMocker(id, phoneChannelMocker);

          ResourceMocker<String, PrintableFormServiceChannel> printableFormChannelMocker = new ResourceMocker<>();
          printableFormChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(String.format(PRINTABLE_FORM_CHANNELS_PATH, id)));
          serviceMocker.addSubMocker(id, printableFormChannelMocker);

          ResourceMocker<String, PrintableFormServiceChannel> locationChannelMocker = new ResourceMocker<>();
          locationChannelMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(String.format(LOCATION_CHANNELS_PATH, id)));
          serviceMocker.addSubMocker(id, locationChannelMocker);

          ResourceMocker<String, WebPageServiceChannel> webPageChannelMocker = new ResourceMocker<>();
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
      ResourceMocker<String, ElectronicServiceChannel> channelMocker = (ResourceMocker<String, ElectronicServiceChannel>) serviceMocker.getSubMocker(serviceId, 0);
      if (!channelMocker.isMocked(id)) {
        channelMocker.add(id, readElectronicServiceChannelFromJSONFile(String.format("electronicservicechannels/%s.json", id)), urlPathEqualTo(String.format(CHANNEL_TEMPLATE, String.format(ELECTRONIC_CHANNELS_PATH, serviceId), id)));
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
      ResourceMocker<String, PhoneServiceChannel> channelMocker = (ResourceMocker<String, PhoneServiceChannel>) serviceMocker.getSubMocker(serviceId, 1);
      if (!channelMocker.isMocked(id)) {
        channelMocker.add(id, readPhoneServiceChannelFromJSONFile(String.format("phonechannels/%s.json", id)), urlPathEqualTo(String.format(CHANNEL_TEMPLATE, String.format(PHONE_CHANNELS_PATH, serviceId), id)));
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
      ResourceMocker<String, PrintableFormServiceChannel> channelMocker = (ResourceMocker<String, PrintableFormServiceChannel>) serviceMocker.getSubMocker(serviceId, 2);
      if (!channelMocker.isMocked(id)) {
        channelMocker.add(id, readPrintableFormServiceChannelFromJSONFile(String.format("printableformchannels/%s.json", id)), urlPathEqualTo(String.format(CHANNEL_TEMPLATE, String.format(PRINTABLE_FORM_CHANNELS_PATH, serviceId), id)));
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
      ResourceMocker<String, ServiceLocationServiceChannel> channelMocker = (ResourceMocker<String, ServiceLocationServiceChannel>) serviceMocker.getSubMocker(serviceId, 3);
      if (!channelMocker.isMocked(id)) {
        channelMocker.add(id, readServiceLocationServiceChannelFromJSONFile(String.format("servicelocationchannels/%s.json", id)), urlPathEqualTo(String.format(CHANNEL_TEMPLATE, String.format(LOCATION_CHANNELS_PATH, serviceId), id)));
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
      ResourceMocker<String, WebPageServiceChannel> channelMocker = (ResourceMocker<String, WebPageServiceChannel>) serviceMocker.getSubMocker(serviceId, 4);
      if (!channelMocker.isMocked(id)) {
        channelMocker.add(id, readWebPageServiceChannelFromJSONFile(String.format("webpagechannels/%s.json", id)), urlPathEqualTo(String.format(CHANNEL_TEMPLATE, String.format(WEBPAGE_CHANNELS_PATH, serviceId), id)));
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
  private ElectronicServiceChannel readElectronicServiceChannelFromJSONFile(String file) {
    return readJSONFile(file, ElectronicServiceChannel.class);
  }
  
  /**
   * Reads JSON file as phone service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private PhoneServiceChannel readPhoneServiceChannelFromJSONFile(String file) {
    return readJSONFile(file, PhoneServiceChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private PrintableFormServiceChannel readPrintableFormServiceChannelFromJSONFile(String file) {
    return readJSONFile(file, PrintableFormServiceChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private ServiceLocationServiceChannel readServiceLocationServiceChannelFromJSONFile(String file) {
    return readJSONFile(file, ServiceLocationServiceChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private WebPageServiceChannel readWebPageServiceChannelFromJSONFile(String file) {
    return readJSONFile(file, WebPageServiceChannel.class);
  }
  
}
