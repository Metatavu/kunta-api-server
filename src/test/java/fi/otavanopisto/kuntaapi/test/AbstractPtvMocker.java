package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;

import ezvcard.util.IOUtils;
import fi.metatavu.ptv.client.model.VmOpenApiItem;
import fi.otavanopisto.kuntaapi.server.persistence.dao.AbstractDAO;

@SuppressWarnings ({"squid:S1166", "squid:S1450"})
public abstract class AbstractPtvMocker<R> {

  private static final String APPLICATION_JSON = "application/json";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String CONTENT_LENGTH = "Content-Length";

  private static Logger logger = Logger.getLogger(AbstractPtvMocker.class.getName());

  private EnumMap<MockedResourceStatus, List<MappingBuilder>> statusLists = new EnumMap<>(MockedResourceStatus.class);
  private boolean started = false;
  private Map<String, MockedResource<String, R>> resources = new LinkedHashMap<>();
  private Map<String, List<AbstractPtvMocker<?>>> subMockers = new LinkedHashMap<>();
  
  public AbstractPtvMocker() {
    mockDefaultLists();
  }
  
  public void start() {
    started = true;
    
    for (MockedResource<String, R> resource : resources.values()) {
      for (MappingBuilder mapping : resource.getCurrentMappings()) {
        stubFor(mapping);
      }
    }
    
    for (Entry<MockedResourceStatus, List<MappingBuilder>> statusListEntry : statusLists.entrySet()) {
      List<MappingBuilder> mappings = statusListEntry.getValue();  
      for (MappingBuilder mapping : mappings) {
        MockedResourceStatus status = statusListEntry.getKey();
        mapping.willReturn(aResponse()
          .withHeader(CONTENT_TYPE, APPLICATION_JSON)
          .withBody(toJSON(getGuidPage(status))));
        stubFor(mapping);
      }
    }
    
    for (List<AbstractPtvMocker<?>> subMockerList : subMockers.values()) {
      for (AbstractPtvMocker<?> subMocker : subMockerList) {
        subMocker.start();
      }
    }
  }
  
  public void stop() {
    if (!started) {
      return;
    }
    
    started = false;
    
    for (List<AbstractPtvMocker<?>> subMockerList : subMockers.values()) {
      for (AbstractPtvMocker<?> subMocker : subMockerList) {
        subMocker.stop();
      }
    }

    for (MockedResource<String, R> resource : resources.values()) {
      for (MappingBuilder mapping : resource.getMappings()) {
        removeStub(mapping);
      }
    }
    
    for (List<MappingBuilder> mappings : statusLists.values()) {
      for (MappingBuilder mapping : mappings) {
        removeStub(mapping);
      }
    }
    
    resources.clear();
    statusLists.clear();
    subMockers.clear();
  }
  
  public abstract String getName();
  public abstract String getBasePath();
  public abstract String getEntityId(R entity);
  
  public void add(String id, R resource, UrlPattern urlPattern) {
    MappingBuilder okGetMapping = createOkGetMapping(urlPattern, resource);
    MappingBuilder okHeadMapping = createOkHeadMapping(urlPattern, resource);
    
    MappingBuilder notFoundMapping = createNotFoundMapping(urlPattern);
    
    resources.put(id, new MockedResource<>(id, resource, okGetMapping, okHeadMapping, notFoundMapping));
  }
  
  public Collection<MockedResource<String, R>> getMockedResources() {
    return resources.values();
  }
  
  public Collection<MockedResource<String, R>> getMockedResources(MockedResourceStatus status) {
    Collection<MockedResource<String, R>> result = new ArrayList<>();
    
    for (MockedResource<String, R> resource : getMockedResources()) {
      if (resource.getStatus().equals(status)) {
        result.add(resource);
      }
    }
    
    return result;
  }
  
  public GuidPage getGuidPage(MockedResourceStatus status) {
    GuidPage result = new GuidPage();
    
    for (MockedResource<String, R> mockedResource : getMockedResources(status)) {
      VmOpenApiItem item = new VmOpenApiItem();
      item.setId(mockedResource.getId());
      item.setName(mockedResource.getId());
      result.addItem(item);
    }
    
    result.setPageCount(1);
    result.setPageNumber(0);
    result.setPageSize(1000);
    
    return result;
  }
  
  public boolean isMocked(String id) {
    return resources.containsKey(id);
  }  
  
  public void setStatus(String id, MockedResourceStatus status) {
    MockedResource<String, R> resource = resources.get(id);
    if (resource.getStatus() == status) {
      return;
    }
    
    if (started && resource.getCurrentMappings() != null) {
      for (MappingBuilder mapping : resource.getCurrentMappings()) {
        removeStub(mapping);
      }
    }
        
    resource.setStatus(status);
    
    if (started) {
      for (MappingBuilder mapping : resource.getCurrentMappings()) {
        stubFor(mapping);
      }
      
      updateStatusLists();
      updateSubMockerStatuses(id, status);
    }
  }
  
  @SuppressWarnings ("squid:S1301")
  public void mockAlternative(String id, R alternative) {
    List<MappingBuilder> alternativeMappings = new ArrayList<>();
    MockedResource<String, R> resource = resources.get(id);
    
    List<MappingBuilder> okMappings = resource.getMappings(MockedResourceStatus.OK);
    for (MappingBuilder okMapping : okMappings) {

      if (started) {
        removeStub(okMapping);
      }
      
      MappingBuilder alternativeMapping = null;
      String method = okMapping.build().getRequest().getMethod().getName();
      
      switch (method) {
        case "GET":
          alternativeMapping = okMapping.willReturn(createOkGetReturn(alternative));
        break;
        case "HEAD":
          alternativeMapping = okMapping.willReturn(createOkHeadReturn(alternative));
        break;
        default:
          fail(String.format("Unknown method %s", method));
        break;
      }
      
      if (started) {
        stubFor(alternativeMapping);
      } 
      
      alternativeMappings.add(alternativeMapping);
    }
    
    resource.setResource(alternative);
    resource.updateStatusMappings(MockedResourceStatus.OK, alternativeMappings);
  }

  public AbstractPtvMocker<R> mock(String... ids) {
    for (String id : ids) {
      try {
        if (!isMocked(id)) {
          mockEntity(id, readEntity(id));
        } else {
          setStatus(id, MockedResourceStatus.OK);
        }
      } catch (JsonProcessingException e) {
        logger.log(Level.SEVERE, () -> String.format("Failed to read %s of %s", id, getName()));
        fail(e.getMessage());
      }
    }
    
    return this;
  }

  public AbstractPtvMocker<R> unmock(String... ids) {
    for (String id : ids) {
      setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  protected void mockEntity(String id, R entity) throws JsonProcessingException {
    add(id, entity, urlPathEqualTo(String.format("%s/%s", getBasePath(), id)));
  }
  
  private void mockDefaultLists() {
    Map<String, StringValuePattern> queryParams = new LinkedHashMap<>();
    queryParams.put("page", containing("0"));
    
    addStatusList(MockedResourceStatus.OK, urlPathEqualTo(getBasePath()));
    addStatusList(MockedResourceStatus.OK, urlPathEqualTo(getBasePath()), queryParams);
  }
  
  private void updateSubMockerStatuses(String id, MockedResourceStatus status) {
    if (subMockers.containsKey(id)) {
      for (AbstractPtvMocker<?> subMocker : subMockers.get(id)) {
        if (status == MockedResourceStatus.OK) {
          subMocker.start();
        } else {
          subMocker.stop();
        }
      }
    }
  }
  
  public void addStatusList(MockedResourceStatus status, UrlPattern urlPattern) {
    addStatusList(status, urlPattern, null);
  }  

  public void addStatusList(MockedResourceStatus status, UrlPattern urlPattern, Map<String, StringValuePattern> queryParams) {
    MappingBuilder mapping = get(urlPattern);
    
    if (queryParams != null) {
      for (Entry<String, StringValuePattern> queryParam : queryParams.entrySet()) {
        mapping = mapping.withQueryParam(queryParam.getKey(), queryParam.getValue());
      }
    }
    
    if (!statusLists.containsKey(status)) {
      statusLists.put(status, new ArrayList<>());
    }
    
    statusLists.get(status).add(mapping);
  }

  public void addSubMocker(String id, AbstractPtvMocker<?> subMocker) {
    if (!subMockers.containsKey(id)) {
      subMockers.put(id, new ArrayList<>());
    }
    
    subMockers.get(id).add(subMocker);
  }
  
  protected R readEntity(String id) {
    return readEntityFromJSONFile(String.format("ptv/%s/%s.json", getName(), id));
  }

  @SuppressWarnings("unchecked")
  private R readEntityFromJSONFile(String file) {
    return readJSONFile(file, getGenericTypeClass());
  }
  
  private R readJSONFile(String file, Class <R> type){
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
      return objectMapper.readValue(stream, type);
    } catch (IOException e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, "Failed to read mock file", e);
      }
      
      fail(e.getMessage());
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  private Class<R> getGenericTypeClass() {
    Type genericSuperclass = getClass().getGenericSuperclass();

    if (genericSuperclass instanceof ParameterizedType) {
      return (Class<R>) getFirstTypeArgument((ParameterizedType) genericSuperclass);
    } else {
      if ((genericSuperclass instanceof Class<?>) && (AbstractDAO.class.isAssignableFrom((Class<?>) genericSuperclass))) {
        return (Class<R>) getFirstTypeArgument((ParameterizedType) ((Class<?>) genericSuperclass).getGenericSuperclass());
      }
    }

    return null;
  }

  private Class<?> getFirstTypeArgument(ParameterizedType parameterizedType) {
    return (Class<?>) parameterizedType.getActualTypeArguments()[0];
  }

  @SuppressWarnings ("squid:S1452")
  public AbstractPtvMocker<?> getSubMocker(String id, int index) {
    return subMockers.get(id).get(index);
  }

  private MappingBuilder createOkHeadMapping(UrlPattern urlPattern, R resource) {
    return head(urlPattern)
      .willReturn(createOkHeadReturn(resource));
  }
  
  private MappingBuilder createOkGetMapping(UrlPattern urlPattern, R resource) {
    return get(urlPattern)
      .willReturn(createOkGetReturn(resource));
  }
  
  private MappingBuilder createNotFoundMapping(UrlPattern urlPattern) {
    return get(urlPattern)
      .willReturn(aResponse()
      .withStatus(404));
  }
  
  private ResponseDefinitionBuilder createOkHeadReturn(R resource) {
    if (resource instanceof File) {
      File file = (File) resource;
      
      try (FileInputStream fileInputStream = new FileInputStream(file)) {
        String contentType = Files.probeContentType(file.toPath());
        byte[] data = IOUtils.toByteArray(fileInputStream);
        return aResponse()
          .withHeader(CONTENT_TYPE, contentType)
          .withHeader(CONTENT_LENGTH, String.valueOf(data.length));
      } catch (IOException e) {
        fail(e.getMessage());
        return null;
      }
      
    } else {
      return aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON);
    }
  }
  
  private ResponseDefinitionBuilder createOkGetReturn(R resource) {
    if (resource instanceof File) {
      File file = (File) resource;
      
      try (FileInputStream fileInputStream = new FileInputStream(file)) {
        String contentType = Files.probeContentType(file.toPath());
        byte[] data = IOUtils.toByteArray(fileInputStream);
        return aResponse()
          .withHeader(CONTENT_TYPE, contentType)
          .withHeader(CONTENT_LENGTH, String.valueOf(data.length))
          .withBody(data);
      } catch (IOException e) {
        fail(e.getMessage());
        return null;
      }
      
    } else {
      return aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withBody(toJSON(resource));
    }
  }
  
  private void updateStatusLists() {
    for (Entry<MockedResourceStatus, List<MappingBuilder>> statusListEntry : statusLists.entrySet()) {
      List<MappingBuilder> mappings = statusListEntry.getValue();  
      for (MappingBuilder mapping : mappings) {
        MockedResourceStatus status = statusListEntry.getKey();
        
        removeStub(mapping);
        
        mapping.willReturn(aResponse()
          .withHeader(CONTENT_TYPE, APPLICATION_JSON)
          .withBody(toJSON(getGuidPage(status))));
        
        stubFor(mapping);
      }
    }
  }
  
  private String toJSON(Object object) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
      return null;
    }
  }
  
  public class GuidPage {

    private Integer pageNumber = null;
    private Integer pageSize = null;
    private Integer pageCount = null;
    private List<VmOpenApiItem> itemList = new ArrayList<>();

    public GuidPage() {
      // Zero-argument constructor
    }

    public GuidPage(Integer pageNumber, Integer pageSize, Integer pageCount, List<VmOpenApiItem> itemList) {
      super();
      this.pageNumber = pageNumber;
      this.pageSize = pageSize;
      this.pageCount = pageCount;
      this.itemList = itemList;
    }

    public Integer getPageNumber() {
      return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
      this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
      return pageSize;
    }

    public void setPageSize(Integer pageSize) {
      this.pageSize = pageSize;
    }

    public Integer getPageCount() {
      return pageCount;
    }

    public void setPageCount(Integer pageCount) {
      this.pageCount = pageCount;
    }

    public List<VmOpenApiItem> getItemList() {
      return itemList;
    }

    public void setItemList(List<VmOpenApiItem> itemList) {
      this.itemList = itemList;
    }
    
    public void addItem(VmOpenApiItem item) {
      this.itemList.add(item);
    }

  }

}
