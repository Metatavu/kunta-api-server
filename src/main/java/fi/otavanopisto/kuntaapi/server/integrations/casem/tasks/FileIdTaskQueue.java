package fi.otavanopisto.kuntaapi.server.integrations.casem.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.casem.CaseMConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractIdTaskQueue;

@ApplicationScoped
public class FileIdTaskQueue extends AbstractIdTaskQueue<FileId> {

  @Override
  public String getSource() {
    return CaseMConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public IdType getType() {
    return IdType.FILE;
  }
  
}