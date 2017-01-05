package fi.otavanopisto.kuntaapi.server.cache;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;

import org.infinispan.manager.CacheContainer;

@ApplicationScoped
public class SystemController {

  @Resource (lookup = "java:jboss/infinispan/container/kunta-api")
  private CacheContainer cacheContainer;
  
  public boolean isCacheContainerOk() {
    return cacheContainer.getCache("announcements") != null;
  }
  
}
