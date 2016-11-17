package fi.otavanopisto.kuntaapi.server.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.IdPair;
import fi.otavanopisto.kuntaapi.server.id.OrganizationBaseId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

public abstract class AbstractEntityRelationCache <P extends BaseId, C extends BaseId, V> extends AbstractCache<IdPair<P, C>, V> {
  
  private static final long serialVersionUID = 3433384306131725962L;
  
  @Inject
  private IdController idController;
  
  @Override
  public void put(IdPair<P, C> id, V response) {
    super.put(createCacheKey(id), response);
  }
  
  @Override
  public V get(IdPair<P, C> id) {
    return super.get(createCacheKey(id));
  }

  public List<IdPair<P, C>> getChildIds(P eventId) {
    Set<IdPair<P, C>> ids = getIds();
    List<IdPair<P, C>> result = new ArrayList<>();
    
    for (IdPair<P, C> id : ids) {
      if (id.parentEquals(eventId)) {
        result.add(id);
      }
    }
    
    return result;
  }
  
  @SuppressWarnings("unchecked")
  private IdPair<P, C> createCacheKey(IdPair<P, C> pair) {
    return new IdPair<>((P) translateId((BaseId) pair.getParent()), (C) translateId((BaseId) pair.getChild()));
  }
  
  private BaseId translateId(BaseId id) {
    BaseId result = idController.translateId(id, KuntaApiConsts.IDENTIFIER_NAME);
    if (result instanceof OrganizationBaseId) {
      OrganizationBaseId organizationBaseId = (OrganizationBaseId) result;
      organizationBaseId.setOrganizationId(idController.translateOrganizationId(organizationBaseId.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME));
    }
    
    return result;
  }
  
}
