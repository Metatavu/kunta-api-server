package fi.metatavu.kuntaapi.server.discover;

public abstract class IdUpdater extends AbstractDiscoverJob {

  @Override
  public long getTestModeTimerInterval() {
    return 1000l;
  }

  @Override
  public long getTestModeTimerWarmup() {
    return 2000l;
  }

  @Override
  public String getSettingPrefix() {
    return "id-updater";
  }

}
