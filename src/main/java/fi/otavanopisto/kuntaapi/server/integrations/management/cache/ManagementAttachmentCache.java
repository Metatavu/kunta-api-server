package fi.otavanopisto.kuntaapi.server.integrations.management.cache;

import fi.otavanopisto.kuntaapi.server.cache.AbstractAttachmentCache;

public class ManagementAttachmentCache extends AbstractAttachmentCache {

  private static final long serialVersionUID = -4688810253543996810L;

  @Override
  public String getCacheName() {
    return "management-attachments";
  }

}
