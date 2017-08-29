package fi.otavanopisto.kuntaapi.test.server.unit.linkedevents;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.otavanopisto.kuntaapi.server.integrations.linkedevents.jackson.LinkedEventsTimeModule;

public class DateTimeDeserializerTest {
  
  @Test
  public void testDeserializeOffsetDateTime() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new LinkedEventsTimeModule());
    TestObject testObject = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("linkedevents/dates.json"), TestObject.class);
    
    assertNotNull(testObject.getWithTime());
    assertNotNull(testObject.getWithoutTime());

    LocalDate withoutTime = LocalDate.from(testObject.getWithoutTime());
    OffsetDateTime withTime = OffsetDateTime.from(testObject.getWithTime());

    assertNotNull(withoutTime);
    assertEquals(2017, withoutTime.getYear());
    assertEquals(9, withoutTime.getMonthValue());
    assertEquals(1, withoutTime.getDayOfMonth());

    assertNotNull(withTime);
    assertEquals(2017, withTime.getYear());
    assertEquals(8, withTime.getMonthValue());
    assertEquals(29, withTime.getDayOfMonth());
    assertEquals(5, withTime.getHour());
    assertEquals(17, withTime.getMinute());
  }
  
  public static class TestObject {

    @JsonProperty("withTime")
    private TemporalAccessor withTime = null;
    
    @JsonProperty("withoutTime")
    private TemporalAccessor withoutTime = null;
    
    public TemporalAccessor getWithoutTime() {
      return withoutTime;
    }
    
    public void setWithoutTime(TemporalAccessor withoutTime) {
      this.withoutTime = withoutTime;
    }
    
    public TemporalAccessor getWithTime() {
      return withTime;
    }
    
    public void setWithTime(TemporalAccessor withTime) {
      this.withTime = withTime;
    }
    
  }

}
