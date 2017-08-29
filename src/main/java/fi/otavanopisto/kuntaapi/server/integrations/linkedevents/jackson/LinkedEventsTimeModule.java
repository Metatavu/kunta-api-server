package fi.otavanopisto.kuntaapi.server.integrations.linkedevents.jackson;

import java.time.temporal.TemporalAccessor;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class LinkedEventsTimeModule extends SimpleModule {
  
  private static final long serialVersionUID = 1401865748212544765L;

  public LinkedEventsTimeModule() {
    addDeserializer(TemporalAccessor.class, new TemporalAccessorDeserializer());  
  }
  
}
