package fi.metatavu.kuntaapi.test.server.unit.discover;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import fi.metatavu.kuntaapi.server.discover.EntityDiscoverJob;

@RunWith (CdiTestRunner.class)
public class EntityDiscoverJobTest {
  
  @Inject
  private Instance<EntityDiscoverJob<?>> entityDiscoverJobs;
  
  @Test
  @SuppressWarnings({"unchecked", "squid:S1160"})
  public void testBeanAnnotations() throws IOException, ClassNotFoundException {
    assertNotNull(entityDiscoverJobs);
    
    Iterator<EntityDiscoverJob<?>> iterator = entityDiscoverJobs.iterator();
    assertTrue(iterator.hasNext());
    
    while (iterator.hasNext()) {
      EntityDiscoverJob<?> entityUpdater = iterator.next();
      assertNotNull(entityUpdater);
      String className = StringUtils.substringBefore(entityUpdater.getClass().getName(), "$");
      Class<? extends EntityDiscoverJob<?>> entityUpdaterClass = (Class<? extends EntityDiscoverJob<?>>) Class.forName(className);
      assertNotNull(entityUpdaterClass);
      
      assertTrue(String.format("Singleton not present in %s", entityUpdaterClass.getName()), entityUpdaterClass.isAnnotationPresent(Singleton.class));
      assertTrue(String.format("ApplicationScoped not present in %s", entityUpdaterClass.getName()), entityUpdaterClass.isAnnotationPresent(ApplicationScoped.class));
      assertTrue(String.format("AccessTimeout not present in %s", entityUpdaterClass.getName()), entityUpdaterClass.isAnnotationPresent(AccessTimeout.class));
    }
  }

}
