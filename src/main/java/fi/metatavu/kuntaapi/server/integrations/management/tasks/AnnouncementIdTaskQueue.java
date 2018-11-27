package fi.metatavu.kuntaapi.server.integrations.management.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.AnnouncementId;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsTaskQueue;

@ApplicationScoped
public class AnnouncementIdTaskQueue extends AbstractJmsTaskQueue<IdTask<AnnouncementId>> {
  
  public static final String NAME = "management-announcements";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }
  
}