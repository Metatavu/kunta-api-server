package fi.otavanopisto.kuntaapi.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CasemMocker extends AbstractMocker {
  
  private static final String APPLICATION_JSON = "application/json";
  private static final String URL_PREFIX = "/casem/api/opennc/v1/";
  
  public CasemMocker mockSubnodes(Integer rootNode, Integer...subNodes) {
    List<String> pathSegments = new ArrayList<>();
    
    pathSegments.add(String.format("Nodes(%d)", rootNode));
    
    if (subNodes != null) {
      for (Integer parent : subNodes) {
        pathSegments.add(String.format("SubNodes(%d)", parent));
      }
    }
    
    pathSegments.add("SubNodes()");
    
    String path = StringUtils.join(pathSegments, "/");
    
    String file = String.format("subnodes/%d.json", subNodes == null || subNodes.length == 0 ? rootNode : subNodes[subNodes.length - 1]);
    
    mockGetString(String.format("%s/%s", URL_PREFIX, path), APPLICATION_JSON, readCasemFile(file));
    
    return this;
  }
  
  public CasemMocker mockContentList() {
    mockGetString(String.format("%s/Contents()", URL_PREFIX), APPLICATION_JSON, readCasemFile("contents.json"));
    mockGetString(String.format("%s/Contents", URL_PREFIX), APPLICATION_JSON, readCasemFile("contents.json"));
    return this;
  }
  
  public CasemMocker mockContent(Integer... contentIds) {
    for (Integer contentId : contentIds) {
      mockGetString(String.format("%s/Contents(%d)", URL_PREFIX, contentId), APPLICATION_JSON, readCasemFile(String.format("contents/%d.json", contentId)));
      mockGetString(String.format("%s/Contents(%d)/ExtendedProperties", URL_PREFIX, contentId), APPLICATION_JSON, readCasemFile(String.format("contents/%d_ext.json", contentId)));
    }
    
    return this;
  }
  
  private String readCasemFile(String path) {
    return readFile(String.format("casem/%s", path));
  }
  
}
