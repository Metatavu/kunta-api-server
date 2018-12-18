package fi.metatavu.kuntaapi.test.server.unit.id;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.reflections.Reflections;

import fi.metatavu.kuntaapi.server.id.BaseId;

public class BaseIdTest {

  @Test
  @SuppressWarnings ("squid:S1166")
  public void testTestUniqueHashSeeds() {
    Reflections reflections = new Reflections("fi.metatavu.kuntaapi.server");
    
    List<BaseId> ids = new ArrayList<>();
    
    for (Class<? extends BaseId> idClass : reflections.getSubTypesOf(BaseId.class)) {
      try {
        if (!Modifier.isAbstract(idClass.getModifiers())) {
          ids.add(idClass.newInstance());
        }
      } catch (InstantiationException | IllegalAccessException e) {
        fail(String.format("Failed to construct %s with message %s", idClass.getName(), e.getMessage()));
      }
    }
    
    assertFalse(ids.isEmpty());
    Map<Integer, String> multiplierOddNumbers = new HashMap<>();
    Map<Integer, String> initialOddNumbers = new HashMap<>();
    
    for (BaseId id : ids) {
      String className = id.getClass().getName();
      
      int initialOddNumber = id.getHashInitial();
      int multiplierOddNumber = id.getHashMultiplier();
      
      assertFalse(String.format("Initial odd number %d used several times (%s, %s)", initialOddNumber, initialOddNumbers.get(initialOddNumber), className), initialOddNumbers.containsKey(initialOddNumber));
      assertFalse(String.format("Multiplier odd number %d used several times (%s, %s)", multiplierOddNumber, multiplierOddNumbers.get(multiplierOddNumber), className), multiplierOddNumbers.containsKey(multiplierOddNumber));
      assertTrue(initialOddNumber % 2 == 1);
      assertTrue(multiplierOddNumber % 2 == 1);
      
      initialOddNumbers.put(initialOddNumber, className);
      multiplierOddNumbers.put(multiplierOddNumber, className);
    }
  }
  
}
