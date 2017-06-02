package fi.otavanopisto.kuntaapi.server.integrations.linkedevents.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractAttachmentResourceContainer;

@ApplicationScoped
public class LinkedEventsAttachmentResourceContainer extends AbstractAttachmentResourceContainer {

  private static final long serialVersionUID = 2244844053439335812L;

  @Override
  public String getName() {
    return "linkedevents-attachments";
  }

}
