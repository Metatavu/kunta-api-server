package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import ezvcard.util.IOUtils;

@SuppressWarnings ("squid:S1166")
public abstract class AbstractResourceMocker<I, R> {

  private static final String APPLICATION_JSON = "application/json";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String CONTENT_LENGTH = "Content-Length";

  private EnumMap<MockedResourceStatus, List<MappingBuilder>> statusLists = new EnumMap<>(MockedResourceStatus.class);
  private boolean started = false;
  private Map<I, MockedResource<I, R>> resources = new LinkedHashMap<>();
  private Map<I, List<AbstractResourceMocker<?, ?>>> subMockers = new LinkedHashMap<>();
  
  public void start() {
    started = true;
    
    for (MockedResource<I, R> resource : resources.values()) {
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
          .withBody(toJSON(getListContent(status))));
        stubFor(mapping);
      }
    }
    
    for (List<AbstractResourceMocker<?, ?>> subMockerList : subMockers.values()) {
      for (AbstractResourceMocker<?, ?> subMocker : subMockerList) {
        subMocker.start();
      }
    }
  }
  
  public void stop() {
    if (!started) {
      return;
    }
    
    started = false;
    
    for (List<AbstractResourceMocker<?, ?>> subMockerList : subMockers.values()) {
      for (AbstractResourceMocker<?, ?> subMocker : subMockerList) {
        subMocker.stop();
      }
    }

    for (MockedResource<I, R> resource : resources.values()) {
      for (MappingBuilder mapping : resource.getMappings()) {
        removeStub(mapping);
      }
    }
    
    for (List<MappingBuilder> mappings : statusLists.values()) {
      for (MappingBuilder mapping : mappings) {
        removeStub(mapping);
      }
    }
  }
  
  public abstract Object getListContent(MockedResourceStatus status);
  
  public void add(I id, R resource, UrlPattern urlPattern) {
    MappingBuilder okGetMapping = createOkGetMapping(urlPattern, resource);
    MappingBuilder okHeadMapping = createOkHeadMapping(urlPattern, resource);
    
    MappingBuilder notFoundMapping = createNotFoundMapping(urlPattern);
    
    resources.put(id, new MockedResource<>(id, resource, okGetMapping, okHeadMapping, notFoundMapping));
  }
  
  public Collection<MockedResource<I, R>> getMockedResources() {
    return resources.values();
  }
  
  public Collection<MockedResource<I, R>> getMockedResources(MockedResourceStatus status) {
    Collection<MockedResource<I, R>> result = new ArrayList<>();
    
    for (MockedResource<I, R> resource : getMockedResources()) {
      if (resource.getStatus().equals(status)) {
        result.add(resource);
      }
    }
    
    return result;
  }
  
  public boolean isMocked(I id) {
    return resources.containsKey(id);
  }  
  
  public void setStatus(I id, MockedResourceStatus status) {
    MockedResource<I, R> resource = resources.get(id);
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
  public void mockAlternative(I id, R alternative) {
    List<MappingBuilder> alternativeMappings = new ArrayList<>();
    MockedResource<I, R> resource = resources.get(id);
    
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

  private void updateSubMockerStatuses(I id, MockedResourceStatus status) {
    if (subMockers.containsKey(id)) {
      for (AbstractResourceMocker<?, ?> subMocker : subMockers.get(id)) {
        if (status == MockedResourceStatus.OK) {
          subMocker.start();
        } else {
          subMocker.stop();
        }
      }
    }
  }
  
  /**
   * Clears status lists from the mocker
   */
  public void clearStatusLists() {
    statusLists.clear();
  }
  
  public void addStatusList(MockedResourceStatus status, UrlPattern urlPattern) {
    addStatusList(status, urlPattern, null);
  }  

  public void addStatusList(MockedResourceStatus status, UrlPattern urlPattern, Map<String, StringValuePattern> queryParams) {
    MappingBuilder mapping = get(urlPattern);
    
    if (queryParams != null) {
      for (Entry<String, StringValuePattern> queryParam : queryParams.entrySet()) {
        mapping.withQueryParam(queryParam.getKey(), queryParam.getValue());
      }
    }
    
    if (!statusLists.containsKey(status)) {
      statusLists.put(status, new ArrayList<>());
    }
    
    statusLists.get(status).add(mapping);
  }

  public void addSubMocker(I id, AbstractResourceMocker<?, ?> subMocker) {
    if (!subMockers.containsKey(id)) {
      subMockers.put(id, new ArrayList<>());
    }
    
    subMockers.get(id).add(subMocker);
  }
  
  @SuppressWarnings ("squid:S1452")
  public AbstractResourceMocker<?, ?> getSubMocker(I id, int index) {
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
          .withBody(toJSON(getListContent(status))));
        
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


}
