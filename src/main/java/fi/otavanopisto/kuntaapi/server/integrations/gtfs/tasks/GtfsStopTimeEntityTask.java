package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import org.onebusaway.gtfs.model.StopTime;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class GtfsStopTimeEntityTask extends AbstractGtfsEntityTask<StopTime> {
  
  private static final long serialVersionUID = -6013833030693616383L;
  
  private OrganizationId organizationId;
  
  public GtfsStopTimeEntityTask() {
    // Zero-argument constructor
  }
  
  public GtfsStopTimeEntityTask(OrganizationId organizationId, StopTime stopTime, Long orderIndex) {
    super(stopTime, orderIndex);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public String getUniqueId() {
    return String.format("gtfs-stoptime-entity-task-%s-%s", getOrganizationId().toString(), getEntity().getId());
  }
  
}