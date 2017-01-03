package fi.otavanopisto.kuntaapi.server.discover;

import fi.otavanopisto.kuntaapi.server.id.ServiceId;

public class ServiceIdUpdateRequest extends AbstractIdUpdateRequest<ServiceId> {

  public ServiceIdUpdateRequest(ServiceId id, Long orderIndex, boolean priority) {
    super(id, orderIndex, priority);
  }

}
