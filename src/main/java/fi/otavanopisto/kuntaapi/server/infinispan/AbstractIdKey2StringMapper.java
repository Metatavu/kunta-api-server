package fi.otavanopisto.kuntaapi.server.infinispan;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationBaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

public abstract class AbstractIdKey2StringMapper implements TwoWayKey2StringMapper {

  private static final String ID_PACKAGE = BaseId.class.getPackage().getName();

  private static final Logger logger = Logger.getLogger(AbstractIdKey2StringMapper.class.getName());

  protected String stringifyOrganizationBaseId(OrganizationBaseId id) {
    return String.format("%s@%s", stringifyBaseId(id), stringifyBaseId(id.getOrganizationId()));
  }
  
  protected String stringifyBaseId(BaseId id) {
    return String.format("%s:%s", id.getClass().getSimpleName(), id.getId());
  }

  protected String stringifyId(Object key) {
    if (key instanceof OrganizationBaseId) {
      return stringifyOrganizationBaseId((OrganizationBaseId) key);
    }
    
    return stringifyBaseId((BaseId) key);
  }
  
  protected BaseId parseId(String string) {
    String[] compositeParts = StringUtils.split(string, '@');
    if (compositeParts.length == 1) {
      String[] idParts = StringUtils.split(compositeParts[0], ':');
      if (idParts.length == 2) {
        return createId(idParts[0], idParts[1]);
      } else {
        logger.severe(String.format("Could not parse id %s expected 2 parts", string));
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
  
  protected BaseId createId(String className, String id) {
    try {
      @SuppressWarnings("unchecked")
      Class<? extends BaseId> idClass = (Class<? extends BaseId>) Class.forName(String.format("%s.%s", ID_PACKAGE, className));
      BaseId result = idClass.newInstance();
      result.setId(id);
      result.setSource(KuntaApiConsts.IDENTIFIER_NAME);
      return result;
    } catch (ClassNotFoundException e) {
      logger.log(Level.SEVERE, String.format("Could not find id class %s", className), e);
    } catch (InstantiationException | IllegalAccessException e) {
      logger.log(Level.SEVERE, String.format("Could not create id class %s", className), e);
    }
    
    return null;
  }
}
