package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import org.onebusaway.gtfs.model.Stop;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class GtfsStopEntityTask extends AbstractGtfsEntityTask<Stop> {
  
  private static final long serialVersionUID = -4602895757755872694L;
  private OrganizationId organizationId;
  
  public GtfsStopEntityTask() {
    // Zero-argument constructor
  }
  
  public GtfsStopEntityTask(OrganizationId organizationId, Stop stop, Long orderIndex) {
    super(stop, orderIndex);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public String getUniqueId() {
    return String.format("gtfs-stop-entity-task-%s-%s", getOrganizationId().toString(), getEntity().getId());
  }
  
}