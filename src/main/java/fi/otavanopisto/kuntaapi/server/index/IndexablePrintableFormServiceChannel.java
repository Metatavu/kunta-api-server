package fi.otavanopisto.kuntaapi.server.index;

public class IndexablePrintableFormServiceChannel extends AbstractIndexableServiceChannel implements Indexable {

  public IndexablePrintableFormServiceChannel() {
    super();
  }

  public IndexablePrintableFormServiceChannel(String serviceChannelId) {
    super(serviceChannelId);
  }
  
  @Override
  public String getType() {
    return "printable-form-service-channel";
  }
  
}
