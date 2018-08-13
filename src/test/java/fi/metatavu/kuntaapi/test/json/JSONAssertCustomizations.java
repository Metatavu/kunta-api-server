package fi.metatavu.kuntaapi.test.json;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.skyscreamer.jsonassert.Customization;

/**
 * JSONAssert customization utils
 * 
 * @author Antti Leppä
 */
public class JSONAssertCustomizations {
  
  /**
   * Customization for marking property to be not nullable
   * 
   * @param path JSON path
   * @return Customization
   */
  public static Customization notNull(String path) {
    return new Customization(path, new NotNullJSONAssertMatcher());
  }

  /**
   * Customization for marking properties to be not nullable
   * 
   * @param paths array JSON paths
   * @return Customizations
   */
  public static Customization[] notNulls(String... paths) {
    return Arrays.stream(paths).map(JSONAssertCustomizations::notNull).collect(Collectors.toList()).toArray(new Customization[0]);
  }

  /**
   * Customization for marking property to be array with given length
   * 
   * @param path JSON path
   * @return Customization
   */
  public static Customization equalLength(String path, int expectedLength) {
    return new Customization(path, new EqualLengthJSONAssertMatcher(expectedLength));
  }
}
