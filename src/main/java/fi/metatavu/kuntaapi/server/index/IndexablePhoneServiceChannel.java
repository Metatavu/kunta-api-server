package fi.metatavu.kuntaapi.server.index;

public class IndexablePhoneServiceChannel extends AbstractIndexableServiceChannel implements Indexable {

  public IndexablePhoneServiceChannel() {
    super();
  }

  public IndexablePhoneServiceChannel(String serviceChannelId) {
    super(serviceChannelId);
  }
  
  @Override
  public String getType() {
    return "phone-service-channel";
  }
  
}
