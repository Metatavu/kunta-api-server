package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import org.onebusaway.gtfs.model.Trip;

import fi.metatavu.kuntaapi.server.id.OrganizationId;

public class GtfsTripEntityTask extends AbstractGtfsEntityTask<Trip> {
  
  private static final long serialVersionUID = 2532282083255658835L;
  
  private OrganizationId organizationId;
  
  public GtfsTripEntityTask() {
    // Zero-argument constructor
  }
  
  public GtfsTripEntityTask(boolean priority, OrganizationId organizationId, Trip trip, Long orderIndex) {
    super(String.format("gtfs-trip-entity-task-%s-%s", organizationId.toString(), trip.getId()), priority, trip, orderIndex);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }

}