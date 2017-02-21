package fi.otavanopisto.kuntaapi.server.integrations.management.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractIdTaskQueue;

@ApplicationScoped
public class PageIdTaskQueue extends AbstractIdTaskQueue<PageId> {

  @Override
  public String getSource() {
    return ManagementConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public IdType getType() {
    return IdType.PAGE;
  }
  
}