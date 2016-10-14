package fi.otavanopisto.kuntaapi.server.discover;

import fi.otavanopisto.kuntaapi.server.integrations.PhoneChannelId;

public class PhoneChannelIdUpdateRequest extends AbstractIdUpdateRequest<PhoneChannelId> {

  public PhoneChannelIdUpdateRequest(PhoneChannelId id, boolean priority) {
    super(id, priority);
  }

}
