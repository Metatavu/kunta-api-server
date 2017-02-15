package fi.otavanopisto.kuntaapi.server.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

import fi.otavanopisto.kuntaapi.server.id.BaseId;

public class IdModule extends SimpleModule {

  private static final long serialVersionUID = 4877692949996803049L;
  private static final String NAME = "CustomIdModule";

  public IdModule() {
    super(NAME);
    addKeyDeserializer(BaseId.class, new IdKeyDeserializer());
    addKeySerializer(BaseId.class, new IdKeySerializer());
    addSerializer(BaseId.class, new IdSerializer());
    addDeserializer(BaseId.class, new IdDeserializer());
  }
  
}
