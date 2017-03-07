package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing tile id
 * 
 * @author Antti Leppä
 */
public class TileId extends OrganizationBaseId {
  
  private static final long serialVersionUID = -3985567956293034832L;

  /**
   * Zero-argument constructor for tile id
   */
  public TileId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public TileId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.TILE;
  }
  
  @Override
  public int getHashInitial() {
    return 139;
  }
  
  @Override
  public int getHashMultiplier() {
    return 151;
  }
  
}
