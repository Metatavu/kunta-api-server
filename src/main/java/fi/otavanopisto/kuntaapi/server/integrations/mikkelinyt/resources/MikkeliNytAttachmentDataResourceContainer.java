package fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt.resources;

import fi.otavanopisto.kuntaapi.server.resources.AbstractAttachmentDataResourceContainer;

public class MikkeliNytAttachmentDataResourceContainer extends AbstractAttachmentDataResourceContainer {

  private static final long serialVersionUID = -5568320318869273350L;

  @Override
  public String getName() {
    return "mikkelinyt-attachments";
  }

}
