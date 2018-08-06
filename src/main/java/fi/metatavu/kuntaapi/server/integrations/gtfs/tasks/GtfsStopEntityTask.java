package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import org.onebusaway.gtfs.model.Stop;

import fi.metatavu.kuntaapi.server.id.OrganizationId;

public class GtfsStopEntityTask extends AbstractGtfsEntityTask<Stop> {
  
  private static final long serialVersionUID = -4602895757755872694L;
  private OrganizationId organizationId;
  
  public GtfsStopEntityTask() {
    // Zero-argument constructor
  }
  
  public GtfsStopEntityTask(boolean priority, OrganizationId organizationId, Stop stop, Long orderIndex) {
    super(String.format("gtfs-stop-entity-task-%s-%s", organizationId.toString(), stop.getId()), priority, stop, orderIndex);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }

}