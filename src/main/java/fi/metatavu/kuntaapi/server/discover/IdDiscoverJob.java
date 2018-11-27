package fi.metatavu.kuntaapi.server.discover;

public abstract class IdDiscoverJob extends AbstractDiscoverJob {

  @Override
  public long getTestModeTimerInterval() {
    return 1000l;
  }

  @Override
  public long getTestModeTimerWarmup() {
    return 30000l;
  }

  @Override
  public String getSettingPrefix() {
    return "id-updater";
  }

}
