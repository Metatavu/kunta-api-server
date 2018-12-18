package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import org.onebusaway.gtfs.model.Agency;

import fi.metatavu.kuntaapi.server.id.OrganizationId;

public class GtfsAgencyEntityTask extends AbstractGtfsEntityTask<Agency> {
  
  private static final long serialVersionUID = -1413738092321206427L;
  
  private OrganizationId organizationId;
  
  public GtfsAgencyEntityTask() {
    // Zero-argument constructor
  }
  
  public GtfsAgencyEntityTask(boolean priority, OrganizationId organizationId, Agency agency, Long orderIndex) {
    super(String.format("gtfs-agency-entity-task-%s-%s", organizationId.toString(), agency.getId()), priority, agency, orderIndex);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
}
