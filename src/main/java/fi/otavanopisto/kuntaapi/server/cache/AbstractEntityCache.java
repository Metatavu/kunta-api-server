package fi.otavanopisto.kuntaapi.server.cache;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationBaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

public abstract class AbstractEntityCache<K extends BaseId, V> extends AbstractCache<K, V> {
  
  private static final long serialVersionUID = 8192317559659671578L;
  
  @Inject
  private IdController idController;
  
  @Override
  public void put(K id, V response) {
    super.put(getCacheId(id), response);
  }
  
  @Override
  public V get(K id) {
    return super.get(getCacheId(id));
  }
  
  public List<K> getOragnizationIds(OrganizationId organizationId) {
    if (!isOrganizationBaseType()) {
      return Collections.emptyList();  
    }
    
    Set<K> ids = getIds();
    
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    
    List<K> result = new ArrayList<>(ids.size());
    for (K id : ids) {
      OrganizationBaseId organizationBaseId = (OrganizationBaseId) id;
      if (organizationBaseId.getOrganizationId().equals(kuntaApiOrganizationId)) {
        result.add(id);
      }
    }
    
    return result;
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
  
  private boolean isOrganizationBaseType() {
    Type[] parameterizedTypes = getParameterizedTypes();
    return OrganizationBaseId.class.isAssignableFrom((Class<?>) parameterizedTypes[0]);
  }
  
}
