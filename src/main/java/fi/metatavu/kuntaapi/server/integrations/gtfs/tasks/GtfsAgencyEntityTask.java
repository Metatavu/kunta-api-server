package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import org.onebusaway.gtfs.model.Agency;

import fi.metatavu.kuntaapi.server.id.OrganizationId;

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
  public String getUniqueId() {
    return String.format("gtfs-agency-entity-task-%s-%s", getOrganizationId().toString(), getEntity().getId());
  }
  
}
