package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import javax.enterprise.context.Dependent;

import org.apache.commons.lang3.ArrayUtils;

import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.AbstractIdProvider;

/**
 * Id provider for GTFS integration
 * 
 * @author Heikki Kurhinen
 */
@Dependent
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
