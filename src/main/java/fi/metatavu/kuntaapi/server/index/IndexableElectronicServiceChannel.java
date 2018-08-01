package fi.metatavu.kuntaapi.server.index;

public class IndexableElectronicServiceChannel extends AbstractIndexableServiceChannel implements Indexable {
  
  public IndexableElectronicServiceChannel() {
    super();
  }

  public IndexableElectronicServiceChannel(String serviceChannelId) {
    super(serviceChannelId);
  }
  
  @Override
  public String getType() {
    return "electronic-service-channel";
  }
  
}
