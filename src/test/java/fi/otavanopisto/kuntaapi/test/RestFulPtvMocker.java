package fi.otavanopisto.kuntaapi.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fi.otavanopisto.restfulptv.client.model.ElectronicChannel;
import fi.otavanopisto.restfulptv.client.model.Organization;
import fi.otavanopisto.restfulptv.client.model.OrganizationService;
import fi.otavanopisto.restfulptv.client.model.PhoneChannel;
import fi.otavanopisto.restfulptv.client.model.PrintableFormChannel;
import fi.otavanopisto.restfulptv.client.model.Service;
import fi.otavanopisto.restfulptv.client.model.ServiceLocationChannel;
import fi.otavanopisto.restfulptv.client.model.WebPageChannel;

public class RestFulPtvMocker extends AbstractMocker {
  
  private List<Organization> organizationsList;
  private List<Service> servicesList;
  private Map<String, List<ElectronicChannel>> servicesElectronicChannelsList;
  private Map<String, List<PhoneChannel>> servicesPhoneChannelsList;
  private Map<String, List<PrintableFormChannel>> printableFormChannelsList;
  private Map<String, List<ServiceLocationChannel>> serviceLocationChannelsList;
  private Map<String, List<WebPageChannel>> webPageChannelsList;
  private Map<String, List<OrganizationService>> organizationServiceList;
  
  public RestFulPtvMocker() {
    organizationsList = new ArrayList<>();
    servicesList = new ArrayList<>();
    servicesElectronicChannelsList = new HashMap<>();
    servicesPhoneChannelsList = new HashMap<>();
    printableFormChannelsList = new HashMap<>();
    serviceLocationChannelsList = new HashMap<>();
    webPageChannelsList = new HashMap<>();
    organizationServiceList = new HashMap<>();
  }

  public RestFulPtvMocker mockOrganizations(String... ids) {
    for (String id : ids) {
      Organization organization = readOrganizationFromJSONFile(String.format("organizations/%s.json", id));
      mockGetJSON(String.format("%s/organizations/%s", AbstractIntegrationTest.BASE_URL, id), organization, null);
      organizationsList.add(organization);
      
      List<OrganizationService> channelList = organizationServiceList.get(id);
      if (channelList == null) {
        organizationServiceList.put(id, new ArrayList<>(ids.length));
      }
    }     
    
    return this;
  }
  
  public RestFulPtvMocker mockServices(String... ids) {
    for (String id : ids) {
      Service service = readServiceFromJSONFile(String.format("services/%s.json", id));
      mockGetJSON(String.format("%s/services/%s", AbstractIntegrationTest.BASE_URL, id), service, null);
      servicesList.add(service);
      
      if (!servicesElectronicChannelsList.containsKey(id)) {
        servicesElectronicChannelsList.put(id, new ArrayList<>());
      }
      
      if (!servicesPhoneChannelsList.containsKey(id)) {
        servicesPhoneChannelsList.put(id, new ArrayList<>());
      }
      
      if (!printableFormChannelsList.containsKey(id)) {
        printableFormChannelsList.put(id, new ArrayList<>());
      }
      
      if (!serviceLocationChannelsList.containsKey(id)) {
        serviceLocationChannelsList.put(id, new ArrayList<>());
      }
      
      if (!webPageChannelsList.containsKey(id)) {
        webPageChannelsList.put(id, new ArrayList<>());
      }
      
    }     
    
    return this;
  }
  
  public RestFulPtvMocker mockElectronicServiceChannels(String serviceId, String... ids) {
    List<ElectronicChannel> channelList = servicesElectronicChannelsList.get(serviceId);
    
    for (String id : ids) {
      ElectronicChannel channel = readElectronicChannelFromJSONFile(String.format("electronicservicechannels/%s.json", id));
      mockGetJSON(String.format("%s/services/%s/electronicChannels/%s", AbstractIntegrationTest.BASE_URL, serviceId, id), channel, null);
      channelList.add(channel);
    }     
    
    servicesElectronicChannelsList.put(serviceId, channelList);
    
    return this;
  }
  
  public RestFulPtvMocker mockPhoneServiceChannels(String serviceId, String... ids) {
    List<PhoneChannel> channelList = servicesPhoneChannelsList.get(serviceId);
    
    for (String id : ids) {
      PhoneChannel channel = readPhoneChannelFromJSONFile(String.format("phonechannels/%s.json", id));
      mockGetJSON(String.format("%s/services/%s/phoneChannels/%s", AbstractIntegrationTest.BASE_URL, serviceId, id), channel, null);
      channelList.add(channel);
    }     
    
    servicesPhoneChannelsList.put(serviceId, channelList);
    
    return this;
  }
  
  public RestFulPtvMocker mockPrintableFormServiceChannels(String serviceId, String... ids) {
    List<PrintableFormChannel> channelList = printableFormChannelsList.get(serviceId);
    for (String id : ids) {
      PrintableFormChannel channel = readPrintableFormChannelFromJSONFile(String.format("printableformchannels/%s.json", id));
      mockGetJSON(String.format("%s/services/%s/printableFormChannels/%s", AbstractIntegrationTest.BASE_URL, serviceId, id), channel, null);
      channelList.add(channel);
    }     
    
    printableFormChannelsList.put(serviceId, channelList);
    
    return this;
  }
  
  public RestFulPtvMocker mockServiceLocationServiceChannels(String serviceId, String... ids) {
    List<ServiceLocationChannel> channelList = serviceLocationChannelsList.get(serviceId);
    for (String id : ids) {
      ServiceLocationChannel channel = readServiceLocationChannelFromJSONFile(String.format("servicelocationchannels/%s.json", id));
      mockGetJSON(String.format("%s/services/%s/serviceLocationChannels/%s", AbstractIntegrationTest.BASE_URL, serviceId, id), channel, null);
      channelList.add(channel);
    }     
    
    serviceLocationChannelsList.put(serviceId, channelList);
    
    return this;
  }
  
  public RestFulPtvMocker mockWebPageServiceChannels(String serviceId, String... ids) {
    List<WebPageChannel> channelList = webPageChannelsList.get(serviceId);
    for (String id : ids) {
      WebPageChannel channel = readWebPageChannelFromJSONFile(String.format("webpagechannels/%s.json", id));
      mockGetJSON(String.format("%s/services/%s/webPageChannels/%s", AbstractIntegrationTest.BASE_URL, serviceId, id), channel, null);
      channelList.add(channel);
    }     
    
    webPageChannelsList.put(serviceId, channelList);
    
    return this;
  }
  
  public RestFulPtvMocker mockOrganizationServices(String organizationId, String... ids) {
    List<OrganizationService> channelList = organizationServiceList.get(organizationId);
    for (String id : ids) {
      OrganizationService channel = readOrganizationServiceFromJSONFile(String.format("organizationservices/%s.json", id));
      mockGetJSON(String.format("%s/organizations/%s/organizationServices/%s", AbstractIntegrationTest.BASE_URL, organizationId, id), channel, null);
      channelList.add(channel);
    }     
    
    organizationServiceList.put(organizationId, channelList);
    
    return this;
  }

  /**
   * Reads JSON file as organization object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  public Organization readOrganizationFromJSONFile(String file) {
    return readJSONFile(file, Organization.class);
  }
  
  /**
   * Reads JSON file as organization object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  public Service readServiceFromJSONFile(String file) {
    return readJSONFile(file, Service.class);
  }
  
  /**
   * Reads JSON file as electronic service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  public ElectronicChannel readElectronicChannelFromJSONFile(String file) {
    return readJSONFile(file, ElectronicChannel.class);
  }
  
  /**
   * Reads JSON file as phone service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  public PhoneChannel readPhoneChannelFromJSONFile(String file) {
    return readJSONFile(file, PhoneChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  public PrintableFormChannel readPrintableFormChannelFromJSONFile(String file) {
    return readJSONFile(file, PrintableFormChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  public ServiceLocationChannel readServiceLocationChannelFromJSONFile(String file) {
    return readJSONFile(file, ServiceLocationChannel.class);
  }

  /**
   * Reads JSON file as printable form service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  public WebPageChannel readWebPageChannelFromJSONFile(String file) {
    return readJSONFile(file, WebPageChannel.class);
  }
  
  /**
   * Reads JSON file as organization service channel object
   * 
   * @param file path to JSON file
   * @return read object
   */
  public OrganizationService readOrganizationServiceFromJSONFile(String file) {
    return readJSONFile(file, OrganizationService.class);
  }
  
  @Override
  public void startMock() {
    Map<String, String> pageQuery = new HashMap<>();
    pageQuery.put("firstResult", "0");
    pageQuery.put("maxResults", "20");

    mockGetJSON(String.format("%s/organizations", AbstractIntegrationTest.BASE_URL), organizationsList, pageQuery);
    mockGetJSON(String.format("%s/organizations", AbstractIntegrationTest.BASE_URL), organizationsList, null);

    mockGetJSON(String.format("%s/services", AbstractIntegrationTest.BASE_URL), servicesList, pageQuery);
    mockGetJSON(String.format("%s/services", AbstractIntegrationTest.BASE_URL), servicesList, null);
    
    for (Entry<String, List<ElectronicChannel>> channelsEntry : servicesElectronicChannelsList.entrySet()) {
      mockGetJSON(String.format("%s/services/%s/electronicChannels", AbstractIntegrationTest.BASE_URL, channelsEntry.getKey()), channelsEntry.getValue(), null);
    }
    
    for (Entry<String, List<PhoneChannel>> channelsEntry : servicesPhoneChannelsList.entrySet()) {
      mockGetJSON(String.format("%s/services/%s/phoneChannels", AbstractIntegrationTest.BASE_URL, channelsEntry.getKey()), channelsEntry.getValue(), null);
    }

    for (Entry<String, List<PrintableFormChannel>> channelsEntry : printableFormChannelsList.entrySet()) {
      mockGetJSON(String.format("%s/services/%s/printableFormChannels", AbstractIntegrationTest.BASE_URL, channelsEntry.getKey()), channelsEntry.getValue(), null);
    }

    for (Entry<String, List<ServiceLocationChannel>> channelsEntry : serviceLocationChannelsList.entrySet()) {
      mockGetJSON(String.format("%s/services/%s/serviceLocationChannels", AbstractIntegrationTest.BASE_URL, channelsEntry.getKey()), channelsEntry.getValue(), null);
    }

    for (Entry<String, List<WebPageChannel>> channelsEntry : webPageChannelsList.entrySet()) {
      mockGetJSON(String.format("%s/services/%s/webPageChannels", AbstractIntegrationTest.BASE_URL, channelsEntry.getKey()), channelsEntry.getValue(), null);
    }

    for (Entry<String, List<OrganizationService>> organizationServiceEntry : organizationServiceList.entrySet()) {
      mockGetJSON(String.format("%s/organizations/%s/organizationServices", AbstractIntegrationTest.BASE_URL, organizationServiceEntry.getKey()), organizationServiceEntry.getValue(), null);
    }

    super.startMock();
  }
}