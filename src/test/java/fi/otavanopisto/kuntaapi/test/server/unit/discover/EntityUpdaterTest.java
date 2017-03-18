package fi.otavanopisto.kuntaapi.test.server.unit.discover;

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

import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;

@RunWith (CdiTestRunner.class)
public class EntityUpdaterTest {
  
  @Inject
  private Instance<EntityUpdater> entityUpdaters;
  
  @Test
  @SuppressWarnings({"unchecked", "squid:S1160"})
  public void testBeanAnnotations() throws IOException, ClassNotFoundException {
    assertNotNull(entityUpdaters);
    
    Iterator<EntityUpdater> iterator = entityUpdaters.iterator();
    assertTrue(iterator.hasNext());
    
    while (iterator.hasNext()) {
      EntityUpdater entityUpdater = iterator.next();
      assertNotNull(entityUpdater);
      String className = StringUtils.substringBefore(entityUpdater.getClass().getName(), "$");
      Class<? extends EntityUpdater> entityUpdaterClass = (Class<? extends EntityUpdater>) Class.forName(className);
      assertNotNull(entityUpdaterClass);
      
      assertTrue(String.format("Singleton not present in %s", entityUpdaterClass.getName()), entityUpdaterClass.isAnnotationPresent(Singleton.class));
      assertTrue(String.format("ApplicationScoped not present in %s", entityUpdaterClass.getName()), entityUpdaterClass.isAnnotationPresent(ApplicationScoped.class));
      assertTrue(String.format("AccessTimeout not present in %s", entityUpdaterClass.getName()), entityUpdaterClass.isAnnotationPresent(AccessTimeout.class));
    }
  }

}
