package fi.otavanopisto.kuntaapi.server.integrations.casem;

import javax.enterprise.context.Dependent;

import org.apache.commons.lang3.ArrayUtils;

import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.AbstractIdProvider;


/**
 * Id provider for Case M
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@Dependent
public class CaseMIdProvider extends AbstractIdProvider {
  
  private static final IdType[] SUPPORTED_TYPES = new IdType[] {
      IdType.PAGE, 
      IdType.FILE
    };
    
    @Override
    public String getSource() {
      return CaseMConsts.IDENTIFIER_NAME;
    }

    @Override
    public boolean isSupportedType(IdType type) {
      return ArrayUtils.contains(SUPPORTED_TYPES, type);
    }
  

}
