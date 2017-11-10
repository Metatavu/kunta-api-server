package fi.otavanopisto.kuntaapi.server.integrations.ptv.translation;

import fi.metatavu.kuntaapi.server.rest.model.Address;

public class AbstractTranslator {
  
  protected static final String[] WEEKDAY_INDICES = new String[] {
    "Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday"
  };

  /**
   * Returns address PTV subtype for address
   * 
   * @param address address 
   * @return PTV subtype for address
   */
  protected PtvAddressSubtype getAddressSubtype(Address address) {
    return getAddressSubtype(address.getSubtype());
  }

  /**
   * Returns address PTV subtype for address
   * 
   * @param address address 
   * @return PTV subtype for address
   */
  protected PtvAddressSubtype getAddressSubtype(String ptvSubtype) {
    for (PtvAddressSubtype ptvAddressSubtype : PtvAddressSubtype.values()) {
      if (ptvAddressSubtype.getPtvValue().equals(ptvSubtype)) {
        return ptvAddressSubtype;
      }
    }
    
    return PtvAddressSubtype.UNKNOWN;
  }
  
}
