package fi.otavanopisto.kuntaapi.server.index;

public class IndexableWebPageServiceChannel extends AbstractIndexableServiceChannel implements Indexable {

  public IndexableWebPageServiceChannel() {
    super();
  }

  public IndexableWebPageServiceChannel(String serviceChannelId) {
    super(serviceChannelId);
  }
  @Override
  public String getType() {
    return "web-page-service-channel";
  }
  
}
