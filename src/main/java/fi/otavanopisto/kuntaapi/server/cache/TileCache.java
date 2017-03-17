package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;
import fi.metatavu.kuntaapi.server.rest.model.Tile;

@ApplicationScoped
public class TileCache extends AbstractResourceContainer<TileId, Tile> {

  private static final long serialVersionUID = -2662290081567923472L;

  @Override
  public String getName() {
    return "tiles";
  }
  
  @Override
  public String getEntityType() {
    return "resource";
  }

}
