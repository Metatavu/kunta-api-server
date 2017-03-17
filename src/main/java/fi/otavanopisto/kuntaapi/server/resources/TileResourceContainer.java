package fi.otavanopisto.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.metatavu.kuntaapi.server.rest.model.Tile;

@ApplicationScoped
public class TileResourceContainer extends AbstractResourceContainer<TileId, Tile> {

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
