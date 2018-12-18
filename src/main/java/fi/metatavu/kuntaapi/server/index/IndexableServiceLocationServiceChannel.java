package fi.metatavu.kuntaapi.server.index;

public class IndexableServiceLocationServiceChannel extends AbstractIndexableServiceChannel implements Indexable {
  
  public IndexableServiceLocationServiceChannel() {
    super();
  }

  public IndexableServiceLocationServiceChannel(String serviceChannelId) {
    super(serviceChannelId);
  }
  
  @Override
  public String getType() {
    return "service-location-service-channel";
  }

}
