package fi.metatavu.kuntaapi.server.index;

public interface Indexable {

  public String getType();
  
  public String getId();
  
  public Long getOrderIndex();
  
}
