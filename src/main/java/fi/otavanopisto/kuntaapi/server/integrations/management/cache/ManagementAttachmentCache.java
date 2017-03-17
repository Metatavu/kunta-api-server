package fi.otavanopisto.kuntaapi.server.integrations.management.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractAttachmentResourceContainer;

@ApplicationScoped
public class ManagementAttachmentCache extends AbstractAttachmentResourceContainer {

  private static final long serialVersionUID = -4688810253543996810L;

  @Override
  public String getName() {
    return "management-attachments";
  }

}
