package fi.otavanopisto.kuntaapi.server.integrations;

import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.rest.model.FileDef;

/**
 * Interface that describes a single file provider
 * 
 * @author Antti Leppä
 */
public interface FileProvider {
  
  /**
   * Finds a single organization file
   * 
   * @param organizationId organization id
   * @param fileId file id
   * @return file or null of not found
   */
  public FileDef findOrganizationFile(OrganizationId organizationId, FileId fileId);
  
  /**
   * Returns organization file data
   * 
   * @param organizationId organization id
   * @param fileId file id
   * @return organization file data
   */
  public AttachmentData getOrganizationFileData(OrganizationId organizationId, FileId fileId);

}
