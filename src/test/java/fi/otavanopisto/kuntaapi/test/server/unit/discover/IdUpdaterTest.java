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

import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;

@RunWith (CdiTestRunner.class)
public class IdUpdaterTest {
  
  @Inject
  private Instance<IdUpdater> idUpdaters;
  
  @Test
  @SuppressWarnings({"unchecked", "squid:S1160"})
  public void testBeanAnnotations() throws IOException, ClassNotFoundException {
    assertNotNull(idUpdaters);
    
    Iterator<IdUpdater> iterator = idUpdaters.iterator();
    assertTrue(iterator.hasNext());
    
    while (iterator.hasNext()) {
      IdUpdater idUpdater = iterator.next();
      assertNotNull(idUpdater);
      String className = StringUtils.substringBefore(idUpdater.getClass().getName(), "$");
      Class<? extends IdUpdater> idUpdaterClass = (Class<? extends IdUpdater>) Class.forName(className);
      assertNotNull(idUpdaterClass);
      
      assertTrue(String.format("Singleton not present in %s", idUpdaterClass.getName()), idUpdaterClass.isAnnotationPresent(Singleton.class));
      assertTrue(String.format("ApplicationScoped not present in %s", idUpdaterClass.getName()), idUpdaterClass.isAnnotationPresent(ApplicationScoped.class));
      assertTrue(String.format("AccessTimeout not present in %s", idUpdaterClass.getName()), idUpdaterClass.isAnnotationPresent(AccessTimeout.class));
    }
  }

}
