package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import org.onebusaway.gtfs.model.StopTime;

import fi.metatavu.kuntaapi.server.id.OrganizationId;

public class GtfsStopTimeEntityTask extends AbstractGtfsEntityTask<StopTime> {
  
  private static final long serialVersionUID = -6013833030693616383L;
  
  private OrganizationId organizationId;
  
  public GtfsStopTimeEntityTask() {
    // Zero-argument constructor
  }
  
  public GtfsStopTimeEntityTask(boolean priority, OrganizationId organizationId, StopTime stopTime, Long orderIndex) {
    super(String.format("gtfs-stoptime-entity-task-%s-%s", organizationId.toString(), stopTime.getId()), priority, stopTime, orderIndex);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  
}