package fi.metatavu.kuntaapi.server.integrations.management.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.BannerId;
import fi.metatavu.kuntaapi.server.id.IdType;
import fi.metatavu.kuntaapi.server.integrations.management.ManagementConsts;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractIdTaskQueue;

@ApplicationScoped
public class BannerIdTaskQueue extends AbstractIdTaskQueue<BannerId> {

  @Override
  public String getSource() {
    return ManagementConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public IdType getType() {
    return IdType.BANNER;
  }
  
}