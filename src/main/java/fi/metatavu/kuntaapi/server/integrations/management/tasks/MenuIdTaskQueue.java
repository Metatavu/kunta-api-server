package fi.metatavu.kuntaapi.server.integrations.management.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.MenuId;
import fi.metatavu.kuntaapi.server.id.IdType;
import fi.metatavu.kuntaapi.server.integrations.management.ManagementConsts;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractIdTaskQueue;

@ApplicationScoped
public class MenuIdTaskQueue extends AbstractIdTaskQueue<MenuId> {

  @Override
  public String getSource() {
    return ManagementConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public IdType getType() {
    return IdType.MENU;
  }
  
}