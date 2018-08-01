package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

import fi.metatavu.kuntaapi.server.integrations.ptv.CodeType;
import fi.metatavu.kuntaapi.server.tasks.AbstractTask;

public class PtvCodeListTask extends AbstractTask {
  
  private static final long serialVersionUID = 1617145788854073496L;
  
  private CodeType type;
  private Integer page;
  
  public PtvCodeListTask() {
    // Zero-argument constructor
  }
  
  public PtvCodeListTask(CodeType type, Integer page) {
    this.type = type;
    this.page = page;
  }
  
  public Integer getPage() {
    return page;
  }
  
  public CodeType getType() {
    return type;
  }
  
  @Override
  public String getUniqueId() {
    return String.format("ptv-code-list-%s-%d", type, page);
  }
  
}
