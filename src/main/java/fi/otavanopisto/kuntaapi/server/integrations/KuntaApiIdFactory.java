package fi.otavanopisto.kuntaapi.server.integrations;

import fi.otavanopisto.kuntaapi.server.id.AbstractIdFactory;
import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

public class KuntaApiIdFactory extends AbstractIdFactory {

  @Override
  public String getSource() {
    return KuntaApiConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public <T extends BaseId> T createFromIdentifier(Class<T> idClass, Identifier identifier) {
    if(identifier.getOrganizationKuntaApiId() == null) {
      return createId(idClass, null, identifier.getKuntaApiId());
    } else {
      OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getOrganizationKuntaApiId());
      return createId(idClass, organizationId, identifier.getKuntaApiId());
    }
  }
  
}
