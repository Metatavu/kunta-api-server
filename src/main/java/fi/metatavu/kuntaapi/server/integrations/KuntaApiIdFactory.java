package fi.metatavu.kuntaapi.server.integrations;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.AbstractIdFactory;
import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;

@ApplicationScoped
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
