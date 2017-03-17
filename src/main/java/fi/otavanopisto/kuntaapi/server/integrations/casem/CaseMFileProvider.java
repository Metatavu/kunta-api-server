package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.FileDef;
import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.BinaryResponse;
import fi.otavanopisto.kuntaapi.server.integrations.casem.resources.CasemFileResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.FileProvider;

/**
 * File provider for CaseM
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class CaseMFileProvider implements FileProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;
  
  @Inject
  private CasemFileResourceContainer casemFileCache;
  
  @Inject
  private CaseMFileController caseMFileController;

  @Override
  public FileDef findOrganizationFile(OrganizationId organizationId, FileId fileId) {
    return casemFileCache.get(fileId);
  }

  @Override
  public AttachmentData getOrganizationFileData(OrganizationId organizationId, FileId fileId) {
    FileId caseMFileId = idController.translateFileId(fileId, CaseMConsts.IDENTIFIER_NAME);
    if (caseMFileId == null) {
      logger.severe(String.format("FileId %s could not be translated into CaseM id", fileId.toString()));
      return null; 
    }
    
    BinaryResponse response = caseMFileController.downloadFile(caseMFileId);
    if (response == null) {
      return null;
    }
    
    return new AttachmentData(response.getType(), response.getData());
  }
  
}
