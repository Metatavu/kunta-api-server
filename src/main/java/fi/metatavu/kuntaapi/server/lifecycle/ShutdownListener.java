package fi.metatavu.kuntaapi.server.lifecycle;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ShutdownListener implements ServletContextListener {
  
  @Inject
  private Event<BeforeShutdownEvent> beforeShutdownEvent;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    beforeShutdownEvent.fire(new BeforeShutdownEvent());
  }

}
