package fi.otavanopisto.kuntaapi.server.discover;

import fi.otavanopisto.kuntaapi.server.integrations.ElectronicServiceChannelId;

public class ElectronicServiceChannelIdUpdateRequest extends AbstractIdUpdateRequest<ElectronicServiceChannelId> {

  public ElectronicServiceChannelIdUpdateRequest(ElectronicServiceChannelId id, boolean priority) {
    super(id, priority);
  }

}
