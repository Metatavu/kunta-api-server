package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

import fi.metatavu.kuntaapi.server.integrations.ptv.CodeType;
import fi.metatavu.metaflow.tasks.impl.DefaultTaskImpl;

public class PtvCodeListTask extends DefaultTaskImpl {
  
  private static final long serialVersionUID = 1617145788854073496L;
  
  private CodeType type;
  private Integer page;
  
  public PtvCodeListTask() {
    // Zero-argument constructor
  }
  
  public PtvCodeListTask(boolean priority, CodeType type, Integer page) {
	  super(String.format("ptv-code-list-%s-%d", type, page), priority);
    this.type = type;
    this.page = page;
  }
  
  public Integer getPage() {
    return page;
  }
  
  public CodeType getType() {
    return type;
  }
  
}
