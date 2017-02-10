package fi.otavanopisto.kuntaapi.server.cache;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;

@ApplicationScoped
public abstract class AbstractPageIdMapCache extends AbstractEntityCache<OrganizationId, Map<PageId, BaseId>> {

  private static final long serialVersionUID = -5473625018941962975L;
  
}
