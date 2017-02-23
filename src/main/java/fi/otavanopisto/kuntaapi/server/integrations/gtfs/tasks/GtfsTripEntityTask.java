package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import org.onebusaway.gtfs.model.Trip;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class GtfsTripEntityTask extends AbstractGtfsEntityTask<Trip> {
  
  private static final long serialVersionUID = 2532282083255658835L;
  
  private OrganizationId organizationId;
  
  public GtfsTripEntityTask() {
    // Zero-argument constructor
  }
  
  public GtfsTripEntityTask(OrganizationId organizationId, Trip trip, Long orderIndex) {
    super(trip, orderIndex);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public Object[] getHashParts() {
    return new Object[] { organizationId, getEntity().getId() };
  }
  
  @Override
  public int getTaskHashInitialOddNumber() {
    return 1303;
  }
  
  @Override
  public int getMultiplierOddNumber() {
    return 1305;
  }
  
}