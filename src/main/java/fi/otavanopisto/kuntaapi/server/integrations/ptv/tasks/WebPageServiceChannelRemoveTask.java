package fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks;

import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

public class WebPageServiceChannelRemoveTask extends AbstractServiceChannelTask {

  private static final long serialVersionUID = 1156605593864902531L;
  
  private WebPageServiceChannelId webPageServiceChannelId;

  public WebPageServiceChannelRemoveTask() {
    // Zero-argument constructor
  }

  public WebPageServiceChannelRemoveTask(WebPageServiceChannelId webPageServiceChannelId) {
    super(Operation.REMOVE);
    this.webPageServiceChannelId = webPageServiceChannelId;
  }
  
  public WebPageServiceChannelId getWebPageServiceChannelId() {
    return webPageServiceChannelId;
  }
  
  public void setWebPageServiceChannelId(WebPageServiceChannelId webPageServiceChannelId) {
    this.webPageServiceChannelId = webPageServiceChannelId;
  }
  
}
