package fi.metatavu.kuntaapi.server.integrations.management.resources;

import fi.metatavu.kuntaapi.server.resources.AbstractAttachmentDataResourceContainer;

public class ManagementAttachmentDataResourceContainer extends AbstractAttachmentDataResourceContainer {

  private static final long serialVersionUID = 4461968284356933190L;

  @Override
  public String getName() {
    return "management-attachments";
  }

}
