package fi.metatavu.kuntaapi.server.integrations;

import fi.metatavu.kuntaapi.server.rest.model.FileDef;
import fi.metatavu.kuntaapi.server.id.FileId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;

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

  /**
   * Deletes a file. If file can not be found or provider does not support removal no action should be taken
   * 
   * @param organizationId organization id
   * @param fileId file to be removed
   * @return returns whether file was deleted or not
   */
  public boolean deleteOrganizationFile(OrganizationId organizationId, FileId fileId);

}
