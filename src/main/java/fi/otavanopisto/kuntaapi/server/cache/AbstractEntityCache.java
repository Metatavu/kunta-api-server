package fi.otavanopisto.kuntaapi.server.cache;

import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationBaseId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

public abstract class AbstractEntityCache<K extends BaseId, V> extends AbstractCache<K, V> {
  
  private static final long serialVersionUID = 8192317559659671578L;
  
  @Inject
  private IdController idController;
  
  @Override
  public boolean isStored() {
    return true;
  }
  
  @Override
  public void put(K id, V response) {
    super.put(getCacheId(id), response);
  }
  
  @Override
  public V get(K id) {
    return super.get(getCacheId(id));
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
  
}
