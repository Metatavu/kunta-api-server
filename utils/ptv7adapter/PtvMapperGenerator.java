import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Generates mapper.json from changes.csv
 * 
 * Requires org.apache.commons:commons-csv
 */
public class PtvMapperGenerator {

  public static void main(String[] args) throws IOException, URISyntaxException {
    (new PtvMapperGenerator()).run();
  }
  
  private Map<String, Map<String, Map<String, String>>> result = new HashMap<>();

  private void run() throws IOException, URISyntaxException {
    InputStream resourceAsStream = getClass().getResourceAsStream("changes.csv");
    String data = IOUtils.toString(resourceAsStream, "UTF-8");
    try (CSVParser csvParser = new CSVParser(new StringReader(data), CSVFormat.DEFAULT)) {
      for (CSVRecord csvRecord : csvParser) {
        String type = csvRecord.get(0);
        String l1 = StringUtils.trimToNull(csvRecord.get(1));
        String l2 = StringUtils.trimToNull(csvRecord.get(2));
        String l3 = StringUtils.trimToNull(csvRecord.get(3));
        String mappingPtv7 = csvRecord.get(4);
        String mappingPtv8 = csvRecord.get(5);
        
        List<String> ptv7 = splitMapping(mappingPtv7);
        List<String> ptv8 = splitMapping(mappingPtv8);
        
        if (!ptv7.isEmpty()) {
          if (ptv7.size() != ptv8.size()) {
            throw new RuntimeException("Invalid row: " + csvRecord.toString());
          }
          
          addMapping(type, l1, l2, l3, ptv7, ptv8);
          
        }
      }
      FileWriter fileWriter = new FileWriter("mappings.json");
      fileWriter.write(getObjectMapper().writeValueAsString(result));
      fileWriter.flush();
      fileWriter.close();
    }
  }

  private void addMapping(String type, String l1, String l2, String l3, List<String> ptv7, List<String> ptv8) throws JsonProcessingException {
    List<String> levels = new ArrayList<>();
    levels.add(l1);

    if (StringUtils.isNotBlank(l2)) {
      levels.add(l2);
    }

    if (StringUtils.isNotBlank(l3)) {
      levels.add(l3);
    }
    
    if (!result.containsKey(type)) {
      result.put(type, new HashMap<>());
    }
    
    String property = StringUtils.join(levels, "/");
    
    Map<String, Map<String, String>> typeMap = result.get(type);
    if (!typeMap.containsKey(property)) {
      typeMap.put(property, new HashMap<>());
    }
    
    Map<String, String> propertyMap = typeMap.get(property);

    for (int i = 0; i < ptv7.size(); i++) {
      String from = ptv8.get(i);
      String to = ptv7.get(i);
      
      if (!StringUtils.equals(from, to)) {
        propertyMap.put(from, to);
      }
    }
  }

  private List<String> splitMapping(String mapping) {
    return Arrays.stream(StringUtils.split(normalizeMapping(mapping), ",")).map(StringUtils::trim).collect(Collectors.toList()); 
  }
  
  private String normalizeMapping(String mapping) {
    String result = StringUtils.trim(StringUtils.substring(mapping, mapping.indexOf(":") + 1));
    result = StringUtils.strip(result, ".");
    result = StringUtils.replace(result, " or ", ",");
    result = StringUtils.replace(result, " and ", ",");
    return result;
  }

  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    return objectMapper;
  }
}
