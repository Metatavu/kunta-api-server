package fi.otavanopisto.kuntaapi.server.index.search;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.index.IndexableElectronicServiceChannel;

@ApplicationScoped
public class ElectronicServiceChannelSearcher extends AbstractServiceChannelSearcher<IndexableElectronicServiceChannel, ElectronicServiceChannelId> {
  
}
