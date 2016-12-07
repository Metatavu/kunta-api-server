package fi.otavanopisto.kuntaapi.server.discover;

public abstract class EntityEvictor {
  
  public abstract void startTimer();
  
  public abstract void stopTimer();
  
  public abstract String getName();  
  
}
