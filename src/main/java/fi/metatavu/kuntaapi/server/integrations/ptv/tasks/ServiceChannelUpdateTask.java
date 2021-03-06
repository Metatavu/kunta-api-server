package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;

public class ServiceChannelUpdateTask extends AbstractServiceChannelTask {

  private static final long serialVersionUID = 7655207491312350262L;

  private Long orderIndex;
  private String id;

  public ServiceChannelUpdateTask() {
    // Zero-argument constructor
  }

  public ServiceChannelUpdateTask(boolean priority, String id, Long orderIndex) {
    super( String.format("ptv-service-channel-update-task-%s", id), priority, Operation.UPDATE);
    this.id = id;
    this.orderIndex = orderIndex;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }

}
