package fi.otavanopisto.kuntaapi.server.discover;

@SuppressWarnings ("squid:S1610")
public abstract class IdUpdater {
  
  public abstract void startTimer();
  
  public abstract void stopTimer();
  
  public abstract String getName();
  
}
