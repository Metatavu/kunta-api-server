package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;

@ApplicationScoped
public abstract class AbstractPrintableFormServiceChannelResourceContainer extends AbstractResourceContainer<PrintableFormServiceChannelId, PrintableFormServiceChannel> {

  private static final long serialVersionUID = -1552057887296936576L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}