package fi.otavanopisto.kuntaapi.server.discover;

import fi.otavanopisto.kuntaapi.server.integrations.ServiceLocationChannelId;

public class ServiceLocationChannelIdUpdateRequest extends AbstractIdUpdateRequest<ServiceLocationChannelId> {

  public ServiceLocationChannelIdUpdateRequest(ServiceLocationChannelId id, boolean priority) {
    super(id, priority);
  }

}
