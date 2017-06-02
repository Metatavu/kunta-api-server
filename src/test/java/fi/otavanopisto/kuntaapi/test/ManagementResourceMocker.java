package fi.otavanopisto.kuntaapi.test;

import java.util.ArrayList;
import java.util.List;

public class ManagementResourceMocker<I, R> extends AbstractResourceMocker<I, R> {

  @Override
  public Object getListContent(MockedResourceStatus status) {
    List<R> result = new ArrayList<>();
    
    for (MockedResource<I, R> mockedResource : getMockedResources(status)) {
      result.add(mockedResource.getResource());
    }
    
    return result;
  }
  
}
