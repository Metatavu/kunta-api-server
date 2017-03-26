package fi.otavanopisto.kuntaapi.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import com.github.tomakehurst.wiremock.client.MappingBuilder;

public class MockedResource<T> {
  
  private T resource;
  private MockedResourceStatus status;
  private EnumMap<MockedResourceStatus, List<MappingBuilder>> mappings;
  
  public MockedResource(MockedResourceStatus status, T resource, MappingBuilder okGetMapping, MappingBuilder okHeadMapping, MappingBuilder notFoundMapping) {
    this.status = status;
    this.resource = resource;
    this.mappings = new EnumMap<>(MockedResourceStatus.class);
    this.mappings.put(MockedResourceStatus.OK, Arrays.asList(okGetMapping, okHeadMapping));
    this.mappings.put(MockedResourceStatus.NOT_FOUND, Arrays.asList(notFoundMapping));
  }
  
  public MockedResource(T resource, MappingBuilder okGetMapping, MappingBuilder okHeadMapping, MappingBuilder notFoundMapping) {
    this(MockedResourceStatus.OK, resource, okGetMapping, okHeadMapping, notFoundMapping);
  }
  
  public MockedResourceStatus getStatus() {
    return status;
  }
  
  public void setStatus(MockedResourceStatus status) {
    this.status = status;
  }
  
  public List<MappingBuilder> getMappings(MockedResourceStatus status) {
    return this.mappings.get(status);
  }
  
  public List<MappingBuilder> getCurrentMappings() {
    return getMappings(getStatus());
  }
  
  public List<MappingBuilder> getMappings() {
    List<MappingBuilder> result = new ArrayList<>();
    
    for (List<MappingBuilder> mappingBuilders : mappings.values()) {
      result.addAll(mappingBuilders);
    }
    
    return result;
  }
  
  public T getResource() {
    return resource;
  }

  public void setResource(T resource) {
    this.resource = resource;
  }
  
  public void updateStatusMappings(MockedResourceStatus mockedStatus, List<MappingBuilder> mappings) {
    this.mappings.put(mockedStatus, mappings);
  }
  
}