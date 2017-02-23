package fi.otavanopisto.kuntaapi.test.server.unit.tasks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.reflections.Reflections;

import fi.otavanopisto.kuntaapi.server.tasks.AbstractTask;

public class AbstractTaskTest {

  @Test
  public void testTestUniqueHashSeeds() {
    Reflections reflections = new Reflections("fi.otavanopisto.kuntaapi.server");
    
    List<AbstractTask> abstractTasks = new ArrayList<>();
    
    for (Class<? extends AbstractTask> abstractTypeClass : reflections.getSubTypesOf(AbstractTask.class)) {
      try {
        if (!Modifier.isAbstract(abstractTypeClass.getModifiers())) {
          abstractTasks.add(abstractTypeClass.newInstance());
        }
      } catch (InstantiationException | IllegalAccessException e) {
        fail(String.format("Failed to construct %s with message %s", abstractTypeClass.getName(), e.getMessage()));
      }
    }
    
    assertFalse(abstractTasks.isEmpty());
    Map<Integer, String> multiplierOddNumbers = new HashMap<>();
    Map<Integer, String> initialOddNumbers = new HashMap<>();
    
    for (AbstractTask abstractTask : abstractTasks) {
      String className = abstractTask.getClass().getName();
      
      int initialOddNumber = abstractTask.getTaskHashInitialOddNumber();
      int multiplierOddNumber = abstractTask.getMultiplierOddNumber();
      
      assertFalse(String.format("Initial odd number %d used several times (%s, %s)", initialOddNumber, initialOddNumbers.get(initialOddNumber), className), initialOddNumbers.containsKey(initialOddNumber));
      assertFalse(String.format("Multiplier odd number %d used several times (%s, %s)", multiplierOddNumber, multiplierOddNumbers.get(multiplierOddNumber), className), multiplierOddNumbers.containsKey(multiplierOddNumber));
      
      initialOddNumbers.put(initialOddNumber, className);
      multiplierOddNumbers.put(multiplierOddNumber, className);
    }
  }
  
}
