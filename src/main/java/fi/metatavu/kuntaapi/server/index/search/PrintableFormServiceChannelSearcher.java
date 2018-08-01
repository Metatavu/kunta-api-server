package fi.metatavu.kuntaapi.server.index.search;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.index.IndexablePrintableFormServiceChannel;

@ApplicationScoped
public class PrintableFormServiceChannelSearcher extends AbstractServiceChannelSearcher<IndexablePrintableFormServiceChannel, PrintableFormServiceChannelId> {
  
}
