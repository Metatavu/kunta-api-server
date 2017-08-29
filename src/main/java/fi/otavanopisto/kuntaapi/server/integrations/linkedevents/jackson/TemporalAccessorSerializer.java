package fi.otavanopisto.kuntaapi.server.integrations.linkedevents.jackson;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class TemporalAccessorSerializer extends StdSerializer<TemporalAccessor> {

  private static final long serialVersionUID = 7135587749442359793L;

  public TemporalAccessorSerializer() {
    super(TemporalAccessor.class);
  }
  
  @Override
  public void serialize(TemporalAccessor value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if (value != null) {
      if (value.isSupported(ChronoField.HOUR_OF_DAY)) {
        gen.writeString(OffsetDateTime.from(value).toString());
      } else {
        LocalDate localDate = LocalDate.from(value);
        gen.writeString(localDate.toString());
      }
    }
  }


}
