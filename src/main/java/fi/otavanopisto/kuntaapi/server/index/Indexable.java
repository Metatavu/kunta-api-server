package fi.otavanopisto.kuntaapi.server.index;

public interface Indexable {

  public String getType();
  
  public String getId();
  
  public Long getOrderIndex();
  
}
