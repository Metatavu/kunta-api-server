package fi.metatavu.kuntaapi.test;

public class AbstractBaseMocker extends AbstractMocker {

  private boolean mocking = false;

  @Override
  public void startMock() {
    mocking = true;
  }
  
  @Override
  public void endMock() {
    mocking = false;
  }
  
  public boolean isMocking() {
    return mocking;
  }
    
}
