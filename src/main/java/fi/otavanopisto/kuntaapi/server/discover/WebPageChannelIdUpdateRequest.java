package fi.otavanopisto.kuntaapi.server.discover;

import fi.otavanopisto.kuntaapi.server.integrations.WebPageChannelId;

public class WebPageChannelIdUpdateRequest extends AbstractIdUpdateRequest<WebPageChannelId> {

  public WebPageChannelIdUpdateRequest(WebPageChannelId id, boolean priority) {
    super(id, priority);
  }

}
