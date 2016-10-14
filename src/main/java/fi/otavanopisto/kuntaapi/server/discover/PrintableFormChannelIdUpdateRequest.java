package fi.otavanopisto.kuntaapi.server.discover;

import fi.otavanopisto.kuntaapi.server.integrations.PrintableFormChannelId;

public class PrintableFormChannelIdUpdateRequest extends AbstractIdUpdateRequest<PrintableFormChannelId> {

  public PrintableFormChannelIdUpdateRequest(PrintableFormChannelId id, boolean priority) {
    super(id, priority);
  }

}
