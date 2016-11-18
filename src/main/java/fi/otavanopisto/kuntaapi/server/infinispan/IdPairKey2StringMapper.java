package fi.otavanopisto.kuntaapi.server.infinispan;

import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdPair;

public class IdPairKey2StringMapper extends AbstractIdKey2StringMapper {
  
  private static final Logger logger = Logger.getLogger(IdPairKey2StringMapper.class.getName());

  @Override
  public boolean isSupportedType(Class<?> keyType) {
    return IdPair.class.isAssignableFrom(keyType);
  }

  @Override
  public String getStringMapping(Object key) {
    IdPair<?, ?> idPair = (IdPair<?, ?>) key;
    return String.format("%s>%s", stringifyId(idPair.getParent()), stringifyId(idPair.getChild()));
  }

  @Override
  public Object getKeyMapping(String stringKey) {
    String[] parts = StringUtils.split(stringKey, '>');
    if (parts.length != 2) {
      logger.severe("Invalid key: expected 2 parts");
      return null;
    }
    
    return new IdPair<BaseId, BaseId>(parseId(parts[0]), parseId(parts[1]));
  }
  
}
