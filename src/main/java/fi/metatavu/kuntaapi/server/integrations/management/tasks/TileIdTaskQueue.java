package fi.metatavu.kuntaapi.server.integrations.management.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.IdType;
import fi.metatavu.kuntaapi.server.id.TileId;
import fi.metatavu.kuntaapi.server.integrations.management.ManagementConsts;
import fi.metatavu.kuntaapi.server.tasks.AbstractIdTaskQueue;

@ApplicationScoped
public class TileIdTaskQueue extends AbstractIdTaskQueue<TileId> {

  @Override
  public String getSource() {
    return ManagementConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public IdType getType() {
    return IdType.TILE;
  }
  
}