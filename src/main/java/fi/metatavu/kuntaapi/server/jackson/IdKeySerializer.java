package fi.metatavu.kuntaapi.server.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import fi.metatavu.kuntaapi.server.id.BaseId;

public class IdKeySerializer extends StdSerializer<BaseId> {
  
  private static final long serialVersionUID = -6788153795198833247L;

  public IdKeySerializer() {
    super(BaseId.class);
  }
  
  @Override
  public void serialize(BaseId value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    String serialized = fi.metatavu.kuntaapi.server.id.IdSerializer.stringifyId(value);
    gen.writeFieldName(serialized);
  }

}
