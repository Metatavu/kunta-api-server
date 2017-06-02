package fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractAttachmentResourceContainer;

@ApplicationScoped
public class MikkeliNytAttachmentResourceContainer extends AbstractAttachmentResourceContainer {

  private static final long serialVersionUID = 4383727872817770083L;

  @Override
  public String getName() {
    return "mikkelinyt-attachments";
  }

}