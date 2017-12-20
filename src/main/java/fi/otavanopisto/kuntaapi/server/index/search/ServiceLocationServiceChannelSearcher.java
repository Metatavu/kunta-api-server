package fi.otavanopisto.kuntaapi.server.index.search;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.index.IndexableServiceLocationServiceChannel;

@ApplicationScoped
public class ServiceLocationServiceChannelSearcher extends AbstractServiceChannelSearcher<IndexableServiceLocationServiceChannel, ServiceLocationServiceChannelId> {

}
