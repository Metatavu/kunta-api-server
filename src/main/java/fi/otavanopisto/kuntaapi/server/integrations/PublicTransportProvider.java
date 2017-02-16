package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.metatavu.kuntaapi.server.rest.model.Schedule;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportScheduleId;

/**
 * Interface that describes a single public transport provider
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public interface PublicTransportProvider {
  
  /**
   * Lists agencies in an organization
   * 
   * @param organizationId organization id
   * @return organization agencies
   */
  public List<Agency> listAgencies(OrganizationId organizationId);
  
  
  /**
   * Finds single agency in an organization by id
   * 
   * @param organizationId organization id
   * @param agencyId agency id
   * @return agency or null if not found
   */
  public Agency findAgency(OrganizationId organizationId, PublicTransportAgencyId agencyId);
  
  /**
   * Lists schedule in an organization
   * 
   * @param organizationId organization id
   * @return organization schedules
   */
  public List<Schedule> listSchedules(OrganizationId organizationId);
  
  /**
   * Finds single schedule in an organization by id
   * 
   * @param organizationId organization id
   * @param scheduleId schedule id
   * @return schedule or null if not found
   */
  public Schedule findSchedule(OrganizationId organizationId, PublicTransportScheduleId scheduleId);
  
}