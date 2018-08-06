package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;

public class WebPageServiceChannelRemoveTask extends AbstractServiceChannelTask {

  private static final long serialVersionUID = 1156605593864902531L;
  
  private WebPageServiceChannelId webPageServiceChannelId;

  public WebPageServiceChannelRemoveTask() {
    // Zero-argument constructor
  }

  public WebPageServiceChannelRemoveTask(boolean priority, WebPageServiceChannelId webPageServiceChannelId) {
    super(String.format("web-page-service-channel-remove-task-%s", webPageServiceChannelId.toString()), priority, Operation.REMOVE);
    this.webPageServiceChannelId = webPageServiceChannelId;
  }
  
  public WebPageServiceChannelId getWebPageServiceChannelId() {
    return webPageServiceChannelId;
  }
  
  public void setWebPageServiceChannelId(WebPageServiceChannelId webPageServiceChannelId) {
    this.webPageServiceChannelId = webPageServiceChannelId;
  }
  
}
