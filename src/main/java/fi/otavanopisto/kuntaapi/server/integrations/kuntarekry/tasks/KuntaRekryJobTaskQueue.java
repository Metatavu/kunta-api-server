package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.KuntaRekryConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTaskQueue;

@ApplicationScoped
public class KuntaRekryJobTaskQueue extends AbstractTaskQueue<AbstractKuntaRekryJobTask> {

  @Override
  public String getName() {
    return String.format("%s-job", KuntaRekryConsts.IDENTIFIER_NAME);
  }
  
}
