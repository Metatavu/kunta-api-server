package fi.metatavu.kuntaapi.server.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.StoredResourceController;
import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;

@SuppressWarnings ("squid:S1948")
public abstract class AbstractBinaryResourceContainer<K extends BaseId> extends AbstractResourceContainerBase implements Serializable {
  
  private static final long serialVersionUID = 1744385470271720259L;
  
  @Inject
  private Logger logger;
  
  @Inject
  private StoredResourceController storedResourceController;
  
  public void put(K id, StoredBinaryData data) {
    if (data == null) {
      clear(id);
    } else {
      storedResourceController.updateBinaryData(getEntityType(), id, data);
    }
  }

  public void put(K id, AttachmentData attachmentData) {
    try (InputStream dataStream = new ByteArrayInputStream(attachmentData.getData())) {
      put(id, new StoredBinaryData(attachmentData.getType(), dataStream));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed stream binary data", e);
    }
  }

  public StoredBinaryData get(K id) {
    return storedResourceController.getBinaryData(getEntityType(), id);
  }

  public void clear(K id) {
    storedResourceController.updateData(getEntityType(), id, null);
  }
  
}
