package fi.otavanopisto.kuntaapi.server.index.search;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.index.IndexablePrintableFormServiceChannel;

@ApplicationScoped
public class PrintableFormServiceChannelSearcher extends AbstractServiceChannelSearcher<IndexablePrintableFormServiceChannel, PrintableFormServiceChannelId> {
  
}
