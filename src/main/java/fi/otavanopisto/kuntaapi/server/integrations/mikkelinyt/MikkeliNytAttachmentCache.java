package fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.cache.AbstractAttachmentCache;

@ApplicationScoped
public class MikkeliNytAttachmentCache extends AbstractAttachmentCache {

  private static final long serialVersionUID = 4383727872817770083L;

  @Override
  public String getCacheName() {
    return "mikkelinyt-attachments";
  }

}
