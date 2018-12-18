package fi.metatavu.kuntaapi.server.jackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import fi.metatavu.kuntaapi.server.id.IdSerializer;

public class IdKeyDeserializer extends KeyDeserializer {

  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) {
    return IdSerializer.parseId(key);
  }

}
