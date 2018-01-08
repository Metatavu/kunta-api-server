package fi.otavanopisto.kuntaapi.test.utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ezvcard.util.IOUtils;
import fi.metatavu.ptv.client.model.V5VmOpenApiOrganizationService;
import fi.metatavu.ptv.client.model.V7VmOpenApiOrganization;
import fi.metatavu.ptv.client.model.V7VmOpenApiService;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceServiceChannel;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.servicechannels.ServiceChannelType;

@SuppressWarnings ({"squid:S106", "squid:S3457", "squid:S1148"})
public class PtvDownload {
  
  private static final String JSON = ".json";
  private static final String PTV = "v7";

  public static void main(String[] args) throws IOException {
    new PtvDownload()
      .downloadCodes();
  }
  
  private PtvDownload downloadCodes() throws MalformedURLException, FileNotFoundException, IOException {
    String[] areaCodeTypes = {"Province", "HospitalRegions", "BusinessRegions"};
    for (String areaCodeType : areaCodeTypes) {
      download(getFullUrl(String.format("/CodeList/GetAreaCodes/type/%s", areaCodeType)), new File(getCodesFolder(), String.format("%s.json", areaCodeType.toLowerCase())));
    }

    download(getFullUrl("/CodeList/GetCountryCodes"), new File(getCodesFolder(), "country.json"));
    download(getFullUrl("/CodeList/GetLanguageCodes"), new File(getCodesFolder(), "language.json"));
    download(getFullUrl("/CodeList/GetMunicipalityCodes"), new File(getCodesFolder(), "municipality.json"));
    download(getFullUrl("/CodeList/GetPostalCodes"), new File(getCodesFolder(), "postal.json"));

    return this;
  }
  
  @SuppressWarnings("unused")
  private void printIds() throws IOException {
    File[] organizationFiles = listJsonFiles(getOrganizationsFolder());
    File[] serviceFiles = listJsonFiles(getServicesFolder());
    File[] serviceChannelFiles = listJsonFiles(getServiceChannelsFolder());
    
    printIdList("ORGANIZATIONS", organizationFiles);
    printIdList("SERVICES", serviceFiles);
    printIdList("SERVICE_CHANNELS", serviceChannelFiles);
    
    for (ServiceChannelType type : ServiceChannelType.values()) {
      printServiceChannelList(type, serviceChannelFiles);
    }
  }
  
  private ServiceChannelType resolveChannelType(String type) {
    switch (type) {
      case "EChannel":
        return ServiceChannelType.ELECTRONIC_CHANNEL;
      case "ServiceLocation":
        return ServiceChannelType.SERVICE_LOCATION;
      case "PrintableForm":
        return ServiceChannelType.PRINTABLE_FORM;
      case "Phone":
        return ServiceChannelType.PHONE;
      case "WebPage":
        return ServiceChannelType.WEB_PAGE;
      default:
        return null;
    }
  }

  private void printIdList(String constName, File[] files) {
    List<String> ids = new ArrayList<>();
    for (File file : files) {
      ids.add(String.format("      \"%s\"",  StringUtils.stripEnd(file.getName(), JSON)));
    }
    
    System.out.println(String.format("\n    public final static String[] %s = {", constName));
    System.out.println(StringUtils.join(ids, ",\n"));
    System.out.println("    };");
  }

  private void printServiceChannelList(ServiceChannelType type, File[] files) throws IOException {
    List<String> ids = new ArrayList<>();
    for (File file : files) {
      ServiceChannelTypeExtract serviceChannelTypeExtract = getObjectMapper().readValue(file, ServiceChannelTypeExtract.class);
      if (resolveChannelType(serviceChannelTypeExtract.getServiceChannelType()) == type) {
        ids.add(String.format("      \"%s\"",  StringUtils.stripEnd(file.getName(), JSON)));
      }
    }
    
    System.out.println(String.format("\n    public final static String[] %s_SERVICE_CHANNELS = {", type.name()));
    System.out.println(StringUtils.join(ids, ",\n"));
    System.out.println("    };");
  }

  @SuppressWarnings("unused")
  private PtvDownload downloadOrganizations() throws IOException {
    File[] organizationFiles = listJsonFiles(getOrganizationsFolder());
    for (File organizationFile : organizationFiles) {
      downloadOrganizationServices(organizationFile);
    }
    
    return this;
  }
  
  private File[] listJsonFiles(File folder) {
    return folder.listFiles((File dir, String name) -> {
      return name.endsWith(JSON);
    });
  }
  
  private File getOrganizationsFolder() {
    return new File(getPtvFolder(), "organizations");
  }

  private File getServicesFolder() {
    return new File(getPtvFolder(), "services");
  }

  private File getServiceChannelsFolder() {
    return new File(getPtvFolder(), "servicechannels");
  }

  private File getCodesFolder() {
    return new File(getPtvFolder(), "codes");
  }
  
  private File getPtvFolder() {
    return new File(System.getProperty("user.dir"), "src/test/resources/ptv");
  }
  
  private void downloadOrganizationServices(File organizationFile) throws IOException, JsonParseException, JsonMappingException {
    V7VmOpenApiOrganization organization = getObjectMapper().readValue(organizationFile, V7VmOpenApiOrganization.class);
    List<V5VmOpenApiOrganizationService> services = organization.getServices();
    for (V5VmOpenApiOrganizationService service : services) {
      downloadService(service);
    }
  }
  
  private ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }
  
  private String getFullUrl(String path) {
    return String.format("https://api.palvelutietovaranto.trn.suomi.fi/api/%s%s", PTV, path);
  }

  private void downloadService(V5VmOpenApiOrganizationService service) throws IOException {
    String serviceId = service.getService().getId().toString();
    File servicesFolder = getServicesFolder();
    String url = getFullUrl(String.format("/Service/%s", serviceId));
    File target = new File(servicesFolder, String.format("%s.json", serviceId));
    if (!target.exists()) {
      try {
        download(url, target);          
        System.out.println(url);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    downloadServiceChannels(target);
  }
  
  private void downloadServiceChannels(File serviceFile) throws IOException {
    V7VmOpenApiService service = new ObjectMapper().readValue(serviceFile, V7VmOpenApiService.class);
    
    List<V7VmOpenApiServiceServiceChannel> serviceChannels = service.getServiceChannels();
    for (V7VmOpenApiServiceServiceChannel serviceChannel : serviceChannels) {
      downloadServiceChannel(serviceChannel);
    }
  }

  private void downloadServiceChannel(V7VmOpenApiServiceServiceChannel serviceChannel) {
    String serviceChannelId = serviceChannel.getServiceChannel().getId().toString();
    File serviceChannelsFolder = getServiceChannelsFolder();
    
    String url = String.format("https://api.palvelutietovaranto.trn.suomi.fi/api/%s/ServiceChannel/%s", PTV, serviceChannelId);
    File target = new File(serviceChannelsFolder, String.format("%s.json", serviceChannelId));
    if (!target.exists()) {
      try {
        download(url, target);          
        System.out.println(url);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println(target.getAbsolutePath());
    }
  }

  private void download(String url, File target) throws IOException, MalformedURLException, FileNotFoundException {
    URLConnection connection = new URL(url).openConnection();
    try (FileOutputStream fileStream = new FileOutputStream(target)) {
      try (InputStream inputStream = connection.getInputStream()) {
        prettyPrintJson(inputStream, fileStream);
      }
    }
  }
  
  private void prettyPrintJson(InputStream inputStream, FileOutputStream fileStream) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
    byte[] bytes = IOUtils.toByteArray(inputStream);
    
    try {
      ObjectMapper objectMapper = getObjectMapper();
      byte[] data = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(objectMapper.readValue(bytes, new TypeReference<HashMap<String, Object>>() { }));
      fileStream.write(data);
    } catch (IOException e) {
      fileStream.write(bytes);
    }
  }
  
  @JsonIgnoreProperties (ignoreUnknown = true)
  public static class ServiceChannelTypeExtract {
    
    private String serviceChannelType;
    
    public String getServiceChannelType() {
      return serviceChannelType;
    }
    
    public void setServiceChannelType(String serviceChannelType) {
      this.serviceChannelType = serviceChannelType;
    }
    
  }
  
}
