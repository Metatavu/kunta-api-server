package fi.metatavu.kuntaapi.test.utils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.metatavu.kuntaapi.server.integrations.ptv.servicechannels.ServiceChannelType;
import fi.metatavu.ptv.client.model.V5VmOpenApiOrganizationService;
import fi.metatavu.ptv.client.model.V9VmOpenApiOrganization;
import fi.metatavu.ptv.client.model.V9VmOpenApiService;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceServiceChannel;
import fi.metatavu.ptv.client.model.VmOpenApiCodeListItem;
import fi.metatavu.ptv.client.model.VmOpenApiItem;

@SuppressWarnings ({"squid:S106", "squid:S3457", "squid:S1148", "squid:S1192", "squid:UnusedPrivateMethod"})
public class PtvDownload {

  private static final String S_JSON = "%s.json";
  private static final boolean PRINT_IDS = true;
  private static final boolean DOWNLOAD_CODES = false;
  private static final boolean DOWNLOAD_RESOURCES = false;
  private static final boolean PURGE_SERVICES = false;
  private static final boolean PURGE_SERVICECHANNELS = false;
  private static final boolean LIMIT_CHANNELS = true;
  
  private static final String JSON = ".json";
  private static final String PTV = "v8";
  private static final int POSTAL_CODE_PAGES = 5;
  private static final int ORGANIZATION_SERVICE_LIMIT = 10;
  private static final int CHANNEL_LIMIT = 10;

  public static void main(String[] args) throws IOException {
    PtvDownload instance = new PtvDownload();

    if (PURGE_SERVICES) {
      instance.purgeServices();
    }

    if (PURGE_SERVICECHANNELS) {
      instance.purgeServiceChannels();
    }

    if (DOWNLOAD_CODES) {
      instance.downloadCodes();
    }
    
    if (DOWNLOAD_RESOURCES) {
      instance.downloadOrganizations();
    }
    
    if (LIMIT_CHANNELS) {
      instance.limitChannels();
    }
    
    if (PRINT_IDS) {
      instance.printIds();
    }
  }

  private void limitChannels() throws IOException {
    EnumMap<ServiceChannelType,List<String>> channelTypeMap = createChannelTypeMap();
    
    for (ServiceChannelType channelType : ServiceChannelType.values()) {
      List<String> channelIds = channelTypeMap.get(channelType);
      for (int i = channelIds.size() - 1; i >= CHANNEL_LIMIT; i--) {
        deleteServiceChannel(channelIds.get(i));
      }
    }
  }

  private void deleteServiceChannel(String channelId) throws IOException {
    File[] serviceFiles = getServicesFolder().listFiles();
    for (int i = 0; i < serviceFiles.length; i++) {
      System.out.println( IOUtils.toString(new FileReader(serviceFiles[i])) );
      removeServiceChannel(serviceFiles[i], channelId);      
    }
    
    File channelFile = new File(getServiceChannelsFolder(), String.format(S_JSON, channelId));
    Files.delete(channelFile.toPath());
  }
  
  private void removeServiceChannel(File serviceFile, String channelId) throws IOException {
    ObjectMapper objectMapper = getObjectMapper();
    V9VmOpenApiService service = objectMapper.readValue(serviceFile, V9VmOpenApiService.class);
    
    service.setServiceChannels(service.getServiceChannels().stream().filter(serviceChannel -> 
      !StringUtils.equals(channelId, serviceChannel.getServiceChannel().getId().toString())
    ).collect(Collectors.toList()));
    
    try (FileOutputStream fileStream = new FileOutputStream(serviceFile)) {
      try (InputStream inputStream = new ByteArrayInputStream(objectMapper.writeValueAsBytes(service))) {
        prettyPrintJson(inputStream, fileStream, V9VmOpenApiService.class);
      }
    }
  }

  private void purgeServices() {
    Arrays.stream(getServicesFolder().listFiles()).forEach(File::delete);
  }

  private void purgeServiceChannels() {
    Arrays.stream(getServiceChannelsFolder().listFiles()).forEach(File::delete);
  }
  
  private PtvDownload downloadCodes() throws MalformedURLException, FileNotFoundException, IOException {
    String[] areaCodeTypes = {"Province", "HospitalRegions", "BusinessRegions"};
    for (String areaCodeType : areaCodeTypes) {
      download(getFullUrl(String.format("/CodeList/GetAreaCodes/type/%s", areaCodeType)), new File(getCodesFolder(), String.format(S_JSON, areaCodeType.toLowerCase())), VmOpenApiCodeListItem.class);
    }

    download(getFullUrl("/CodeList/GetCountryCodes"), new File(getCodesFolder(), "country.json"), VmOpenApiCodeListItem.class);
    download(getFullUrl("/CodeList/GetLanguageCodes"), new File(getCodesFolder(), "language.json"), VmOpenApiCodeListItem.class);
    download(getFullUrl("/CodeList/GetMunicipalityCodes"), new File(getCodesFolder(), "municipality.json"), VmOpenApiCodeListItem.class);
    
    for (int page = 0; page < POSTAL_CODE_PAGES + 1; page++) {
      download(getFullUrl(String.format("/CodeList/GetPostalCodes?page=%d", page)), new File(getCodesFolder(), String.format("postal-%d.json", page)), VmOpenApiCodeListItem.class);
    }
    
    return this;
  }
  
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
    
    System.out.println("\n    public final static String[][] ORGANIZATION_SERVICES = {");
    List<String> organizationServices = new ArrayList<>();

    for (int i = 0; i < organizationFiles.length; i++) {
      File organizationFile = organizationFiles[i];
      organizationServices.add(i, printOrganizationServicesList(organizationFile));
    }

    System.out.println(StringUtils.join(organizationServices, ",\n"));
    System.out.println("    };");
    
    List<EnumMap<ServiceChannelType, List<String>>> servicesTypeChannels = createServiceChannelsTypeMap(serviceFiles);

    for (ServiceChannelType channelType : ServiceChannelType.values()) {
      System.out.println(String.format("\n    public final static String[][] SERVICE_%s_CHANNELS = {", channelType));
      
      List<String> channelTypeIds = new ArrayList<>();
      
      for (int i = 0; i < servicesTypeChannels.size(); i++) {
        StringBuilder typeIds = new StringBuilder();
        
        typeIds.append("      {");
        
        EnumMap<ServiceChannelType, List<String>> serviceTypeChannels = servicesTypeChannels.get(i);
        List<String> idList = serviceTypeChannels.get(channelType);
        if (idList != null) {
          List<String> ids = serviceTypeChannels.get(channelType).stream()
            .map(id -> String.format(" \"%s\"", id))
            .collect(Collectors.toList());
          typeIds.append(StringUtils.join(ids, ","));
        }
        
        typeIds.append(" }");
        
        channelTypeIds.add(typeIds.toString());
      }
      
      System.out.println(StringUtils.join(channelTypeIds, ",\n"));
      System.out.println("    };");
    }
  }
  
  private EnumMap<ServiceChannelType, List<String>> createChannelTypeMap() {
    EnumMap<ServiceChannelType, List<String>> result = new EnumMap<>(ServiceChannelType.class);
    
    Arrays.stream(ServiceChannelType.values()).forEach(type -> result.put(type, new ArrayList<>()) );
    
    Arrays.stream(getServiceChannelsFolder().listFiles()).forEach(channelFile -> {
      try {
        ServiceChannelType channelType = resolveChannelType(channelFile);
        String id = FilenameUtils.getBaseName(channelFile.getName());
        result.get(channelType).add(id);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    
    return result;
  }

  private List<EnumMap<ServiceChannelType, List<String>>> createServiceChannelsTypeMap(File[] serviceFiles) throws IOException {
    List<EnumMap<ServiceChannelType, List<String>>> result = new ArrayList<>();
    
    for (int i = 0; i < serviceFiles.length; i++) {
      File serviceFile = serviceFiles[i];
      EnumMap<ServiceChannelType, List<String>> channels = new EnumMap<>(ServiceChannelType.class);
      
      V9VmOpenApiService service = new ObjectMapper().readValue(serviceFile, V9VmOpenApiService.class);
      List<V9VmOpenApiServiceServiceChannel> serviceChannels = service.getServiceChannels();
      for (V9VmOpenApiServiceServiceChannel serviceChannel : serviceChannels) {
        File serviceChannelFile = new File(getServiceChannelsFolder(), String.format(S_JSON, serviceChannel.getServiceChannel().getId()));
        ServiceChannelType channelType = resolveChannelType(serviceChannelFile);
        if (!channels.containsKey(channelType)) {
          channels.put(channelType, new ArrayList<>());
        }
        
        channels.get(channelType).add(serviceChannel.getServiceChannel().getId().toString());
      }
      
      result.add(channels);
    }

    return result;
  }

  private ServiceChannelType resolveChannelType(File file) throws IOException {
    ServiceChannelTypeExtract serviceChannelTypeExtract = getObjectMapper().readValue(file, ServiceChannelTypeExtract.class);
    return resolveChannelType(serviceChannelTypeExtract.getServiceChannelType());
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
  
  private String printOrganizationServicesList(File organizationFile) throws IOException {
    ObjectMapper objectMapper = getObjectMapper();
    V9VmOpenApiOrganization organization = objectMapper.readValue(organizationFile, V9VmOpenApiOrganization.class);

    StringBuilder result = new StringBuilder();

    result.append("      {\n");

    List<String> ids = organization.getServices().stream()
      .map(V5VmOpenApiOrganizationService::getService)
      .map(VmOpenApiItem::getId)
      .map(UUID::toString)
      .map(id -> String.format("        \"%s\"", id))
      .collect(Collectors.toList());

    result.append(StringUtils.join(ids, ",\n"));  
    result.append("\n      }");

    return result.toString();
  }
  
  private void printIdList(String constName, File[] files) {
    printIdList(constName, Arrays.stream(files)
      .map(file -> StringUtils.stripEnd(file.getName(), JSON))
      .collect(Collectors.toList()));
  }

  private void printIdList(String constName, List<String> ids) {
    List<String> idTexts = new ArrayList<>();
    for (String id : ids) {
      idTexts.add(String.format("      \"%s\"",  id));
    }
    
    System.out.println(String.format("\n    public final static String[] %s = {", constName));
    System.out.println(StringUtils.join(idTexts, ",\n"));
    System.out.println("    };");
  }

  private void printServiceChannelList(ServiceChannelType type, File[] files) throws IOException {
    List<String> ids = new ArrayList<>();
    for (File file : files) {
      if (resolveChannelType(file) == type) {
        ids.add(String.format("      \"%s\"",  StringUtils.stripEnd(file.getName(), JSON)));
      }
    }
    
    System.out.println(String.format("\n    public final static String[] %s_SERVICE_CHANNELS = {", type.name()));
    System.out.println(StringUtils.join(ids, ",\n"));
    System.out.println("    };");
  }

  private PtvDownload downloadOrganizations() throws IOException {
    File organizationsFolder = getOrganizationsFolder();
    
    File[] organizationFiles = listJsonFiles(organizationsFolder);
    for (File organizationFile : organizationFiles) {
      String organizationId = FilenameUtils.getBaseName(organizationFile.getName());
      downloadOrganization(organizationsFolder, organizationId);
      limitOrganizationServices(organizationFile);
      downloadOrganizationServices(organizationFile);
    }
    
    return this;
  }
  
  private void downloadOrganization(File organizationsFolder, String organizationId) throws IOException {
    String url = getFullUrl(String.format("/Organization/%s", organizationId));
    File target = new File(organizationsFolder, String.format(S_JSON, organizationId));
    Files.deleteIfExists(target.toPath());
    
    try {
      download(url, target, V9VmOpenApiOrganization.class);         
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private File[] listJsonFiles(File folder) {
    return folder.listFiles((File dir, String name) -> {
      return name.endsWith(JSON);
    });
  }
  
  private File getOrganizationsFolder() {
    return new File(getPtvFolder(), "out/organizations");
  }

  private File getServicesFolder() {
    return new File(getPtvFolder(), "out/services");
  }

  private File getServiceChannelsFolder() {
    return new File(getPtvFolder(), "out/servicechannels");
  }

  private File getCodesFolder() {
    return new File(getPtvFolder(), "codes");
  }
  
  private File getPtvFolder() {
    return new File(System.getProperty("user.dir"), "src/test/resources/ptv");
  }
  
  private void limitOrganizationServices(File organizationFile) throws IOException {
    ObjectMapper objectMapper = getObjectMapper();
    V9VmOpenApiOrganization organization = objectMapper.readValue(organizationFile, V9VmOpenApiOrganization.class);
    List<V5VmOpenApiOrganizationService> services = organization.getServices().stream().limit(ORGANIZATION_SERVICE_LIMIT).collect(Collectors.toList());
    organization.setServices(services);
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(organizationFile, organization);
  }
  
  private void downloadOrganizationServices(File organizationFile) throws IOException {
    V9VmOpenApiOrganization organization = getObjectMapper().readValue(organizationFile, V9VmOpenApiOrganization.class);
    
    List<V5VmOpenApiOrganizationService> services = organization.getServices();
    for (V5VmOpenApiOrganizationService service : services) {
      downloadService(service);
    }
  }
  
  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());    
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    return objectMapper;
  }
  
  private String getFullUrl(String path) {
    return String.format("https://api.palvelutietovaranto.trn.suomi.fi/api/%s%s", PTV, path);
  }

  private void downloadService(V5VmOpenApiOrganizationService service) throws IOException {
    String serviceId = service.getService().getId().toString();
    File servicesFolder = getServicesFolder();

    String url = getFullUrl(String.format("/Service/%s", serviceId));
    File target = new File(servicesFolder, String.format(S_JSON, serviceId));
    if (!target.exists()) {
      try {
        download(url, target, V9VmOpenApiService.class);          
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    downloadServiceChannels(target);
  }
  
  private void downloadServiceChannels(File serviceFile) throws IOException {
    V9VmOpenApiService service = new ObjectMapper().readValue(serviceFile, V9VmOpenApiService.class);
    
    List<V9VmOpenApiServiceServiceChannel> serviceChannels = service.getServiceChannels();
    for (V9VmOpenApiServiceServiceChannel serviceChannel : serviceChannels) {
      downloadServiceChannel(serviceChannel);
    }
  }

  private void downloadServiceChannel(V9VmOpenApiServiceServiceChannel serviceChannel) {
    String serviceChannelId = serviceChannel.getServiceChannel().getId().toString();
    File serviceChannelsFolder = getServiceChannelsFolder();
    
    String url = String.format("https://api.palvelutietovaranto.trn.suomi.fi/api/%s/ServiceChannel/%s", PTV, serviceChannelId);
    File target = new File(serviceChannelsFolder, String.format(S_JSON, serviceChannelId));
    if (!target.exists()) {
      try {
        download(url, target, null);    
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void download(String url, File target, Class<?> outputClass) throws IOException {      
    System.out.println(url);
    
    URLConnection connection = new URL(url).openConnection();
    try (FileOutputStream fileStream = new FileOutputStream(target)) {
      try (InputStream inputStream = connection.getInputStream()) {
        prettyPrintJson(inputStream, fileStream, outputClass);
      }
    }
  }
  
  private void prettyPrintJson(InputStream inputStream, FileOutputStream fileStream, Class<?> outputClass) throws IOException {
    byte[] bytes = IOUtils.toByteArray(inputStream);
    
    try {
      ObjectMapper objectMapper = getObjectMapper();
      ObjectWriter prettyPrinter = objectMapper.writerWithDefaultPrettyPrinter();
      byte[] data = null;
          
      if (outputClass == null) {
        data = prettyPrinter.writeValueAsBytes(objectMapper.readValue(bytes, new TypeReference<HashMap<String, Object>>() { }));
      } else {
        data = prettyPrinter.writeValueAsBytes(objectMapper.readValue(bytes, outputClass));
      }
          
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
