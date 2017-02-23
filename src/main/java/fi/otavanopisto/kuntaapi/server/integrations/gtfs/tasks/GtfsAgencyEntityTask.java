package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import org.onebusaway.gtfs.model.Agency;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class GtfsAgencyEntityTask extends AbstractGtfsEntityTask<Agency> {
  
  private static final long serialVersionUID = -1413738092321206427L;
  
  private OrganizationId organizationId;
  
  public GtfsAgencyEntityTask() {
    // Zero-argument constructor
  }
  
  public GtfsAgencyEntityTask(OrganizationId organizationId, Agency agency, Long orderIndex) {
    super(agency, orderIndex);
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
    return 1193;
  }

  @Override
  public int getMultiplierOddNumber() {
    return 1195;
  }
}
