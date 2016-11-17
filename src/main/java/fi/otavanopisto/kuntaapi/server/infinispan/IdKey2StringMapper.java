package fi.otavanopisto.kuntaapi.server.infinispan;

import fi.otavanopisto.kuntaapi.server.id.BaseId;

public class IdKey2StringMapper extends AbstractIdKey2StringMapper {

  @Override
  public boolean isSupportedType(Class<?> keyType) {
    return BaseId.class.isAssignableFrom(keyType);
  }

  @Override
  public String getStringMapping(Object key) {
    return stringifyId((BaseId) key);
  }

  @Override
  public Object getKeyMapping(String stringKey) {
    return parseId(stringKey);
  }
  
}
