package fi.otavanopisto.kuntaapi.server.cache;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.StoredResourceController;
import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationBaseId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

public abstract class AbstractEntityCache<K extends BaseId, V> extends AbstractCache<K, V> {
  
  private static final long serialVersionUID = 8192317559659671578L;
  
  @Inject
  private Logger logger;
  
  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private IdController idController;
  
  @Inject
  private StoredResourceController storedResourceController;
  
  @Override
  public boolean isStored() {
    return true;
  }
  
  public abstract String getEntityType();
  
  @Override
  public void put(K id, V response) {
    String json = toJSON(response);
    if (json != null) {
      storedResourceController.updateData(getEntityType(), id, json);
    }
    
    if (!systemSettingController.isEntityCacheWritesDisabled()) {
      super.put(getCacheId(id), response);
    } else {
      logger.log(Level.INFO, "Entity cache writes are disabled");
    }
  }
  
  @Override
  public V get(K id) {
    if (!systemSettingController.isEntityCacheReadsDisabled()) {
      V result = super.get(getCacheId(id));
      if (result != null) {
        return result;
      }
    }
    
    String storedData = storedResourceController.getData(getEntityType(), id);
    if (storedData != null) {
      return fromJSON(storedData);
    }
    
    return null;
  }

  @SuppressWarnings("unchecked")
  private K getCacheId(K id) {
    K cacheId = (K) idController.translateId(id, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (id instanceof OrganizationBaseId) {
      OrganizationBaseId organizationBaseId = (OrganizationBaseId) cacheId;
      organizationBaseId.setOrganizationId(idController.translateOrganizationId(organizationBaseId.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME));
    } 
    
    return cacheId;
  }
  
  @Override
  public void clear(K id) {
    storedResourceController.updateData(getEntityType(), id, null);
    if (!systemSettingController.isEntityCacheWritesDisabled()) {
      super.clear(id);
    } else {
      logger.log(Level.INFO, "Entity cache writes are disabled");
    }
  }
  
}
