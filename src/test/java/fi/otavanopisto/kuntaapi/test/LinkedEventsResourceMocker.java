package fi.otavanopisto.kuntaapi.test;

import java.util.ArrayList;
import java.util.List;

public class LinkedEventsResourceMocker<I, R> extends AbstractResourceMocker<I, R> {

  @Override
  public Object getListContent(MockedResourceStatus status) {
    List<R> data = new ArrayList<>();

    for (MockedResource<I, R> mockedResource : getMockedResources(status)) {
      data.add(mockedResource.getResource());
    }

    return new ListResult(data);
  }

  public class ListResult {

    private List<R> data;
    private ListResultMeta meta;
    
    public ListResult(List<R> data) {
      this.data = data;
      this.meta = new ListResultMeta(data.size(), null, null);
    }


    public List<R> getData() {
      return data;
    }
    
    public ListResultMeta getMeta() {
      return meta;
    }

  }

  public class ListResultMeta {

    private Integer count;
    private String next;
    private String previous;

    public ListResultMeta(Integer count, String next, String previous) {
      super();
      this.count = count;
      this.next = next;
      this.previous = previous;
    }

    public Integer getCount() {
      return count;
    }

    public String getNext() {
      return next;
    }

    public String getPrevious() {
      return previous;
    }

  }

}
