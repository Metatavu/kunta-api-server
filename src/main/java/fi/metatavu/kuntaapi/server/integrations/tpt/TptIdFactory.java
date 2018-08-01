package fi.metatavu.kuntaapi.server.integrations.tpt;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.AbstractIdFactory;

/**
 * Id factory for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TptIdFactory extends AbstractIdFactory {
  
  @Override
  public String getSource() {
    return TptConsts.IDENTIFIER_NAME;
  }
  
}
