package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.DownloadMeta;
import fi.otavanopisto.kuntaapi.server.integrations.FileProvider;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.rest.model.FileDef;

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
  private CaseMFileController caseMFileController;

  @Override
  public FileDef findOrganizationFile(OrganizationId organizationId, FileId fileId) {
    FileId caseMFileId = idController.translateFileId(fileId, CaseMConsts.IDENTIFIER_NAME);
    if (caseMFileId == null) {
      logger.severe(String.format("FileId %s could not be translated into CaseM id", fileId.toString()));
      return null; 
    }
    
    DownloadMeta meta = caseMFileController.getDownloadMeta(caseMFileId);
    if (meta == null) {
      return null;
    }
    
    return translateFile(caseMFileId, meta);
  }

  @Override
  public AttachmentData getOrganizationFileData(OrganizationId organizationId, FileId fileId) {
    // TODO Auto-generated method stub
    return null;
  }
  
  private FileDef translateFile(FileId fileId, DownloadMeta meta) {
    FileId kuntaApiFileId = idController.translateFileId(fileId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiFileId == null) {
      logger.severe(String.format("FileId %s could not be translated into Kunta API id", fileId.toString()));
      return null; 
    }
    
    FileDef file = new FileDef();
    file.setContentType(meta.getContentType());
    file.setId(kuntaApiFileId.getId());
    file.setSize(meta.getSize() != null ? meta.getSize().longValue() : null);
    file.setSlug(meta.getFilename());
    file.setTitle(meta.getFilename());
    
    return file;
  }
}
