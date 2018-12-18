package fi.metatavu.kuntaapi.server.integrations.linkedevents.resources;

import fi.metatavu.kuntaapi.server.resources.AbstractAttachmentDataResourceContainer;

public class LinkedEventsAttachmentDataResourceContainer extends AbstractAttachmentDataResourceContainer {

  private static final long serialVersionUID = 5719505469704254811L;

  @Override
  public String getName() {
    return "linkedevents-attachments";
  }

}
