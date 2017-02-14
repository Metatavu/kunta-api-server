package fi.otavanopisto.kuntaapi.server.jackson;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdSerializer;

public class IdDeserializer extends JsonDeserializer<BaseId> {

  @Override
  public BaseId deserialize(JsonParser jsonParser, DeserializationContext deserializationContetx) throws IOException {
    JsonToken currentToken = jsonParser.getCurrentToken();

    if (currentToken.equals(JsonToken.VALUE_STRING)) {
      String text = jsonParser.getText().trim();
      if (StringUtils.isNotBlank(text)) {
        return IdSerializer.parseId(text);
      }
      
      throw deserializationContetx.weirdStringException(text, BaseId.class, String.format("%s could not be deserialized", text));
    } else if (currentToken.equals(JsonToken.VALUE_NULL)) {
      return getNullValue(deserializationContetx);
    }

    throw deserializationContetx.mappingException(Boolean.class);
  }

}
