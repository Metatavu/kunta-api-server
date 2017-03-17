package fi.otavanopisto.kuntaapi.server.integrations.management.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractAttachmentResourceContainer;

@ApplicationScoped
public class ManagementAttachmentResourceContainer extends AbstractAttachmentResourceContainer {

  private static final long serialVersionUID = -4688810253543996810L;

  @Override
  public String getName() {
    return "management-attachments";
  }

}
