package fi.otavanopisto.kuntaapi.server.jackson;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

public class UnknownFieldLoggerDeserializationProblemHandler extends DeserializationProblemHandler {
  
  private static final Logger logger = Logger.getLogger(UnknownFieldLoggerDeserializationProblemHandler.class.getName());

  @Override
  public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
    logger.log(Level.WARNING, () -> String.format("Unknown property %s found when deserializing %s", propertyName, beanOrClass));
    return true;
  }
  
}
