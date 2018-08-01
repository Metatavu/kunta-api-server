package fi.metatavu.kuntaapi.server.index.search;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.index.IndexableServiceLocationServiceChannel;

@ApplicationScoped
public class ServiceLocationServiceChannelSearcher extends AbstractServiceChannelSearcher<IndexableServiceLocationServiceChannel, ServiceLocationServiceChannelId> {

}
