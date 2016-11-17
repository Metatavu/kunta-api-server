package fi.otavanopisto.kuntaapi.server.infinispan;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationBaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class IdKey2StringMapper implements TwoWayKey2StringMapper  {
  
  private static final Logger logger = Logger.getLogger(IdKey2StringMapper.class.getName());

  @Override
  public boolean isSupportedType(Class<?> keyType) {
    return BaseId.class.isAssignableFrom(keyType);
  }

  @Override
  public String getStringMapping(Object key) {
    if (key instanceof OrganizationBaseId) {
      return stringifyOrganizationBaseId((OrganizationBaseId) key);
    } else if (key instanceof BaseId) {
      return stringifyBaseId((BaseId) key);
    } else {
      logger.severe(String.format("Could not stringify key %s", key));
      return null;
    }
  }

  @Override
  public Object getKeyMapping(String stringKey) {
    return parseId(stringKey);
  }

  private String stringifyOrganizationBaseId(OrganizationBaseId id) {
    return String.format("%s@%s", stringifyBaseId(id), stringifyBaseId(id.getOrganizationId()));
  }
  
  private String stringifyBaseId(BaseId id) {
    return String.format("%s:%s:%s", id.getClass().getName(), id.getId(), id.getSource());
  }
  
  private BaseId parseId(String string) {
    String[] compositeParts = StringUtils.split(string, '@');
    if (compositeParts.length == 1) {
      String[] idParts = StringUtils.split(compositeParts[0], ':');
      if (idParts.length == 3) {
        return createId(idParts[0], idParts[1], idParts[2]);
      } else {
        logger.severe(String.format("Could not parse id %s expected 3 parts", string));
        return null;
      }
    } else if (compositeParts.length == 2) {
      OrganizationBaseId id = (OrganizationBaseId) parseId(compositeParts[0]);
      id.setOrganizationId((OrganizationId) parseId(compositeParts[1]));
      return id;
    } else {
      logger.severe(String.format("Could not parse id %s expected 1 or 2 parts", string));
      return null;
    }
  }
  
  private BaseId createId(String className, String id, String source) {
    try {
      @SuppressWarnings("unchecked")
      Class<? extends BaseId> idClass = (Class<? extends BaseId>) Class.forName(className);
      BaseId result = idClass.newInstance();
      result.setId(id);
      result.setSource(source);
      return result;
    } catch (ClassNotFoundException e) {
      logger.log(Level.SEVERE, String.format("Could not find id class %s", className), e);
    } catch (InstantiationException | IllegalAccessException e) {
      logger.log(Level.SEVERE, String.format("Could not create id class %s", className), e);
    }
    
    return null;
  }
  
}
