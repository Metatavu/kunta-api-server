package fi.metatavu.kuntaapi.test.json;

import org.skyscreamer.jsonassert.ValueMatcher;

/**
 * Value matcher for JSONAssert that asserts that given value is not null
 * 
 * @author Antti Leppä
 */
public class NotNullJSONAssertMatcher implements ValueMatcher<Object> {

  @SuppressWarnings ("squid:S1221")
  @Override
  public boolean equal(Object o1, Object o2) {
    return o2 != null;
  }

}
