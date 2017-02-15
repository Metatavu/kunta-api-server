package fi.otavanopisto.kuntaapi.server.infinispan;

import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdSerializer;

public class IdKey2StringMapper implements TwoWayKey2StringMapper {

  @Override
  public boolean isSupportedType(Class<?> keyType) {
    return BaseId.class.isAssignableFrom(keyType);
  }

  @Override
  public String getStringMapping(Object key) {
    return IdSerializer.stringifyId((BaseId) key);
  }

  @Override
  public Object getKeyMapping(String stringKey) {
    return IdSerializer.parseId(stringKey);
  }
  
}
