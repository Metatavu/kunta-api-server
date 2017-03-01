package fi.otavanopisto.kuntaapi.test;

import com.github.tomakehurst.wiremock.client.MappingBuilder;

public class ManagementMockedEntity<T> {
  
  private T entity;
  private MappingBuilder entityMapping;
  private MappingBuilder notFoundMapping;
  
  public ManagementMockedEntity(T entity, MappingBuilder entityMapping, MappingBuilder notFoundMapping) {
    this.entity = entity;
    this.entityMapping = entityMapping;
    this.notFoundMapping = notFoundMapping;
  }
  
  public MappingBuilder getEntityMapping() {
    return entityMapping;
  }
  
  public MappingBuilder getNotFoundMapping() {
    return notFoundMapping;
  }
  
  public T getEntity() {
    return entity;
  }
  
}