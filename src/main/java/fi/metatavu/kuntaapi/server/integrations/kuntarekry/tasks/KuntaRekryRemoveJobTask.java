package fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks;

import fi.metatavu.kuntaapi.server.id.JobId;

public class KuntaRekryRemoveJobTask extends AbstractKuntaRekryJobTask {

  private static final long serialVersionUID = -6734986614498108982L;
  
  private JobId kuntaRekryJobId;

  public KuntaRekryRemoveJobTask(boolean priority, JobId kuntaRekryJobId) {
    super(String.format("kuntarekry-remove-job-task-%s", kuntaRekryJobId.toString()), priority);
    this.kuntaRekryJobId = kuntaRekryJobId;
  }

  public JobId getKuntaRekryJobId() {
    return kuntaRekryJobId;
  }

}
