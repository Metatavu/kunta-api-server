package fi.metatavu.kuntaapi.server.integrations.vcard;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.AbstractIdFactory;

@ApplicationScoped
public class VCardIdFactory extends AbstractIdFactory {
  
  @Override
  public String getSource() {
    return VCardConsts.IDENTIFIER_NAME;
  }
  
}
