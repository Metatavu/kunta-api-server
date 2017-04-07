package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks;

import fi.otavanopisto.kuntaapi.server.id.JobId;

public class KuntaRekryRemoveJobTask extends AbstractKuntaRekryJobTask {

  private static final long serialVersionUID = -6734986614498108982L;
  
  private JobId kuntaRekryJobId;

  public KuntaRekryRemoveJobTask(JobId kuntaRekryJobId) {
    super();
    this.kuntaRekryJobId = kuntaRekryJobId;
  }

  public JobId getKuntaRekryJobId() {
    return kuntaRekryJobId;
  }

}
