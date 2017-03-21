package fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractIdTaskQueue;

@ApplicationScoped
public class PrintableFormServiceChannelIdTaskQueue extends AbstractIdTaskQueue<PrintableFormServiceChannelId> {

  @Override
  public String getSource() {
    return PtvConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public IdType getType() {
    return IdType.PRINTABLE_FORM_SERVICE_CHANNEL;
  }
  
}