package fi.metatavu.kuntaapi.server.index.search;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.index.IndexableWebPageServiceChannel;

@ApplicationScoped
public class WebPageServiceChannelSearcher extends AbstractServiceChannelSearcher<IndexableWebPageServiceChannel, WebPageServiceChannelId> {
  
}
