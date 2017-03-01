package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.Assert.fail;

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
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;

@SuppressWarnings ("squid:S1166")
public class ResourceMocker<I, R> {

  private static final String APPLICATION_JSON = "application/json";
  private static final String CONTENT_TYPE = "Content-Type";

  private EnumMap<MockedResourceStatus, MappingBuilder> statusLists = new EnumMap<>(MockedResourceStatus.class);
  private boolean started = false;
  private Map<I, MockedResource<R>> resources = new LinkedHashMap<>();
  private Map<I, List<ResourceMocker<?, ?>>> subMockers = new LinkedHashMap<>();
  
  public void start() {
    started = true;
    
    for (MockedResource<R> resource : resources.values()) {
      stubFor(resource.getCurrentMapping());
    }
    
    for (Entry<MockedResourceStatus, MappingBuilder> statusListEntry : statusLists.entrySet()) {
      MappingBuilder mapping = statusListEntry.getValue();  
      MockedResourceStatus status = statusListEntry.getKey();
      mapping.willReturn(aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withBody(toJSON(getResources(status))));
      stubFor(mapping);
    }
    
    for (List<ResourceMocker<?, ?>> subMockers : subMockers.values()) {
      for (ResourceMocker<?, ?> subMocker : subMockers) {
        subMocker.start();
      }
    }
  }
  
  public void stop() {
    if (!started) {
      return;
    }
    
    started = false;
    
    for (List<ResourceMocker<?, ?>> subMockers : subMockers.values()) {
      for (ResourceMocker<?, ?> subMocker : subMockers) {
        subMocker.stop();
      }
    }

    for (MockedResource<R> resource : resources.values()) {
      for (MappingBuilder mapping : resource.getMappings()) {
        removeStub(mapping);
      }
    }
    for (MappingBuilder list : statusLists.values()) {
      removeStub(list);
    }
  }
  
  public void add(I id, R resource, UrlPattern urlPattern) {
    MappingBuilder mappingBuilder = get(urlPattern);
    
    MappingBuilder okMapping = mappingBuilder
      .willReturn(aResponse()
      .withHeader(CONTENT_TYPE, APPLICATION_JSON)
      .withBody(toJSON(resource)));
    
    MappingBuilder notFoundMapping = get(urlPattern)
      .willReturn(aResponse()
      .withStatus(404));
    
    resources.put(id, new MockedResource<>(resource, okMapping, notFoundMapping));
  }
  
  public Collection<MockedResource<R>> getMockedResources() {
    return resources.values();
  }
  
  public Collection<MockedResource<R>> getMockedResources(MockedResourceStatus status) {
    Collection<MockedResource<R>> result = new ArrayList<>();
    
    for (MockedResource<R> resource : getMockedResources()) {
      if (resource.getStatus().equals(status)) {
        result.add(resource);
      }
    }
    
    return result;
  }
  
  public List<R> getResources(MockedResourceStatus status) {
    List<R> result = new ArrayList<>();
    
    for (MockedResource<R> mockedResource : getMockedResources(status)) {
      result.add(mockedResource.getResource());
    }
    
    return result;
  }
  
  public boolean isMocked(I id) {
    return resources.containsKey(id);
  }  
  
  public void setStatus(I id, MockedResourceStatus status) {
    MockedResource<R> resource = resources.get(id);
    if (resource.getStatus() == status) {
      return;
    }
    
    if (started && resource.getCurrentMapping() != null) {
      removeStub(resource.getCurrentMapping());
    }
        
    resource.setStatus(status);
    
    if (started) {
      stubFor(resource.getCurrentMapping());
      updateStatusLists();
      updateSubMockerStatuses(id, status);
    }
  }

  private void updateSubMockerStatuses(I id, MockedResourceStatus status) {
    if (subMockers.containsKey(id)) {
      for (ResourceMocker<?, ?> subMocker : subMockers.get(id)) {
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
        mapping.withQueryParam(queryParam.getKey(), queryParam.getValue());
      }
    }
    
    statusLists.put(status, mapping);
  }

  public void addSubMocker(I id, ResourceMocker<?, ?> subMocker) {
    if (!subMockers.containsKey(id)) {
      subMockers.put(id, new ArrayList<>());
    }
    
    subMockers.get(id).add(subMocker);
  }
  
  @SuppressWarnings ("squid:S1452")
  public ResourceMocker<?, ?> getSubMocker(I id, int index) {
    return subMockers.get(id).get(index);
  }
  
  private void updateStatusLists() {
    for (Entry<MockedResourceStatus, MappingBuilder> statusListEntry : statusLists.entrySet()) {
      MappingBuilder mapping = statusListEntry.getValue();  
      MockedResourceStatus status = statusListEntry.getKey();
      
      removeStub(mapping);
      
      mapping.willReturn(aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withBody(toJSON(getResources(status))));
      
      stubFor(mapping);
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
