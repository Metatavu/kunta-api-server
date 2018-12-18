package fi.metatavu.kuntaapi.server.integrations.gtfs;

import org.apache.commons.lang3.ArrayUtils;

import fi.metatavu.kuntaapi.server.id.IdType;
import fi.metatavu.kuntaapi.server.integrations.AbstractIdProvider;
import javax.enterprise.context.ApplicationScoped;

/**
 * Id provider for GTFS integration
 * 
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class GtfsIdProvider extends AbstractIdProvider {

  private static final IdType[] SUPPORTED_TYPES = new IdType[] {
    IdType.PUBLIC_TRANSPORT_AGENCY,
    IdType.PUBLIC_TRANSPORT_ROUTE,
    IdType.PUBLIC_TRANSPORT_SCHEDULE,
    IdType.PUBLIC_TRANSPORT_STOP,
    IdType.PUBLIC_TRANSPORT_STOPTIME,
    IdType.PUBLIC_TRANSPORT_TRIP
  };
  
  @Override
  public String getSource() {
    return GtfsConsts.IDENTIFIER_NAME;
  }

  @Override
  public boolean isSupportedType(IdType type) {
    return ArrayUtils.contains(SUPPORTED_TYPES, type);
  }
  
}
