package fi.otavanopisto.kuntaapi.server.cache;

import fi.metatavu.kuntaapi.server.rest.model.FileDef;
import fi.otavanopisto.kuntaapi.server.id.FileId;

public abstract class AbstractFileCache extends AbstractEntityCache<FileId, FileDef> {

  private static final long serialVersionUID = -2414990078298246580L;

}
