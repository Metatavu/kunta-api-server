package fi.otavanopisto.kuntaapi.server.utils;

import java.time.OffsetDateTime;

public class TimeUtils {

  private TimeUtils() {
  }
    
  public static int compareOffsetDateTimes(OffsetDateTime offsetDateTime1, OffsetDateTime offsetDateTime2) {
    if (offsetDateTime1 == offsetDateTime2) {
      return 0;
    }
    
    if (offsetDateTime1 == null) {
      return -1;
    }
    
    if (offsetDateTime2 == null) {
      return 1;
    }
    
    return offsetDateTime1.compareTo(offsetDateTime2);
  }
  
}
