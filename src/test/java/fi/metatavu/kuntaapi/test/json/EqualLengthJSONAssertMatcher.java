package fi.metatavu.kuntaapi.test.json;

import org.json.JSONArray;
import org.skyscreamer.jsonassert.ValueMatcher;

/**
 * Value matcher for JSONAssert that asserts that given value has expected length
 * 
 * @author Antti Leppä
 */
public class EqualLengthJSONAssertMatcher implements ValueMatcher<Object> {
  
  private int expected;
  
  public EqualLengthJSONAssertMatcher(int expected) {
    super();
    this.expected = expected;
  }

  @Override
  public boolean equal(Object o1, Object o2) {
    if (o2 instanceof JSONArray) {
      return ((JSONArray) o2).length() == expected;
    } else {
      throw new AssertionError(String.format("Unexpected class %s", o2.getClass().getName()));
    }
  }

}
