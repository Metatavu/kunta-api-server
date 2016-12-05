package fi.otavanopisto.kuntaapi.server.integrations.casem.model;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import fi.otavanopisto.kuntaapi.server.integrations.casem.CaseMConsts;

public class CaseMJSONDateDeserializer extends JsonDeserializer<LocalDateTime> {

  private static final String PREFIX = "/Date(";
  private static final String POSTFIX = ")/";
  private static final ZoneId TIME_ZONE = ZoneId.of(CaseMConsts.SERVER_TIMEZONE_ID);

  @Override
  public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContetx) throws IOException {
    JsonToken currentToken = jsonParser.getCurrentToken();

    if (currentToken.equals(JsonToken.VALUE_STRING)) {
      String text = jsonParser.getText().trim();
      text = StringUtils.stripStart(text, PREFIX);
      text = StringUtils.stripEnd(text, POSTFIX);

      if (StringUtils.isNumeric(text)) {
        Long timestamp = NumberUtils.createLong(text);
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TIME_ZONE);
      }

      throw deserializationContetx.weirdStringException(text, LocalDateTime.class,
          String.format("%s could not be translated into LocalDateTime", text));
    } else if (currentToken.equals(JsonToken.VALUE_NULL)) {
      return getNullValue(deserializationContetx);
    }

    throw deserializationContetx.mappingException(Boolean.class);
  }

}
