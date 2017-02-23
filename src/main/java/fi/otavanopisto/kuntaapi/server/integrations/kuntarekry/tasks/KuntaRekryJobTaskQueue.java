package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks;

import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.KuntaRekryConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTaskQueue;

public class KuntaRekryJobTaskQueue extends AbstractTaskQueue<KuntaRekryJobEntityTask> {

  @Override
  public String getName() {
    return String.format("%s-job", KuntaRekryConsts.IDENTIFIER_NAME);
  }
  
}
