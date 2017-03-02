package fi.otavanopisto.kuntaapi.test;

import java.util.Collection;
import java.util.EnumMap;

import com.github.tomakehurst.wiremock.client.MappingBuilder;

public class MockedResource<T> {
  
  private T resource;
  private MockedResourceStatus status;
  private EnumMap<MockedResourceStatus, MappingBuilder> mappings;
  
  public MockedResource(MockedResourceStatus status, T resource, MappingBuilder okMapping, MappingBuilder notFoundMapping) {
    this.status = status;
    this.resource = resource;
    this.mappings = new EnumMap<>(MockedResourceStatus.class);
    this.mappings.put(MockedResourceStatus.OK, okMapping);
    this.mappings.put(MockedResourceStatus.NOT_FOUND, notFoundMapping);
  }
  
  public MockedResource(T resource, MappingBuilder okMapping, MappingBuilder notFoundMapping) {
    this(MockedResourceStatus.OK, resource, okMapping, notFoundMapping);
  }
  
  public MockedResourceStatus getStatus() {
    return status;
  }
  
  public void setStatus(MockedResourceStatus status) {
    this.status = status;
  }
  
  public MappingBuilder getMapping(MockedResourceStatus status) {
    return this.mappings.get(status);
  }
  
  public MappingBuilder getCurrentMapping() {
    return getMapping(getStatus());
  }
  
  public Collection<MappingBuilder> getMappings() {
    return mappings.values();
  }
  
  public T getResource() {
    return resource;
  }
  
}