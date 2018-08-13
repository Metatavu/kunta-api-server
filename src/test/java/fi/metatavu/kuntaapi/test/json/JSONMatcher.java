package fi.metatavu.kuntaapi.test.json;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.json.JSONException;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

import net.minidev.json.JSONObject;

/**
 * Hamcrest matcher for comparing JSON strings
 * 
 * @author Antti Leppä
 */
public class JSONMatcher extends DiagnosingMatcher<Object> {
  
  private static Logger logger = Logger.getLogger(JSONMatcher.class.getName());
  
  private String expected;
  private Customization[] customizations;
  
  /**
   * Constructor 
   * 
   * @param expected expected JSON string
   * @param customizations JSONAssert customizations (optional)
   */
  public JSONMatcher(String expected, Customization... customizations) {
    this.expected = expected;
    this.customizations = customizations;
  }
  
  @Override
  public void describeTo(final Description description) {
    description.appendText(expected);
  }
  
  @Override
  protected boolean matches(final Object actualObject, final Description mismatchDescription) {
    String actual = toJSONString(actualObject);
    
    CustomComparator customComparator = new CustomComparator(JSONCompareMode.LENIENT, customizations);
    try {
      JSONCompareResult result = JSONCompare.compareJSON(expected, actual, customComparator);
      
      if (!result.passed()) {
        mismatchDescription.appendText(result.getMessage());
      }
      
      return result.passed();
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }

  }

  @SuppressWarnings("unchecked")
  private String toJSONString(Object actualObject) {
    if (actualObject == null) {
      return null;
    }
    
    if (actualObject instanceof String) {
      return (String) actualObject;
    }
    
    if (actualObject instanceof Map) {
      return JSONObject.toJSONString((Map<String, ? extends Object>) actualObject);
    }
    
    logger.log(Level.WARNING, () -> String.format("Unknown input type %s", actualObject.getClass().getName()));
    
    return null;
  }


}
