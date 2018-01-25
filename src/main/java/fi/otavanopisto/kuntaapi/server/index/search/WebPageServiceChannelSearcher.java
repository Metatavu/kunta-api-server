package fi.otavanopisto.kuntaapi.server.index.search;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.index.IndexableWebPageServiceChannel;

@ApplicationScoped
public class WebPageServiceChannelSearcher extends AbstractServiceChannelSearcher<IndexableWebPageServiceChannel, WebPageServiceChannelId> {
  
}
