package fi.metatavu.kuntaapi.server.index.search;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.metatavu.kuntaapi.server.index.IndexableElectronicServiceChannel;

@ApplicationScoped
public class ElectronicServiceChannelSearcher extends AbstractServiceChannelSearcher<IndexableElectronicServiceChannel, ElectronicServiceChannelId> {
  
}
