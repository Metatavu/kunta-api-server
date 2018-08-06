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

import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;

@RunWith (CdiTestRunner.class)
public class IdDiscoverJobTest {
  
  @Inject
  private Instance<IdDiscoverJob> idDiscoverJobs;
  
  @Test
  @SuppressWarnings({"unchecked", "squid:S1160"})
  public void testBeanAnnotations() throws IOException, ClassNotFoundException {
    assertNotNull(idDiscoverJobs);
    
    Iterator<IdDiscoverJob> iterator = idDiscoverJobs.iterator();
    assertTrue(iterator.hasNext());
    
    while (iterator.hasNext()) {
      IdDiscoverJob idDiscoverJob = iterator.next();
      assertNotNull(idDiscoverJob);
      String className = StringUtils.substringBefore(idDiscoverJob.getClass().getName(), "$");
      Class<? extends IdDiscoverJob> idDiscoverJobClass = (Class<? extends IdDiscoverJob>) Class.forName(className);
      assertNotNull(idDiscoverJobClass);
      
      assertTrue(String.format("Singleton not present in %s", idDiscoverJobClass.getName()), idDiscoverJobClass.isAnnotationPresent(Singleton.class));
      assertTrue(String.format("ApplicationScoped not present in %s", idDiscoverJobClass.getName()), idDiscoverJobClass.isAnnotationPresent(ApplicationScoped.class));
      assertTrue(String.format("AccessTimeout not present in %s", idDiscoverJobClass.getName()), idDiscoverJobClass.isAnnotationPresent(AccessTimeout.class));
    }
  }

}
