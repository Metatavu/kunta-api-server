package fi.otavanopisto.kuntaapi.server.index.search;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.index.IndexablePhoneServiceChannel;

@ApplicationScoped
public class PhoneServiceChannelSearcher extends AbstractServiceChannelSearcher<IndexablePhoneServiceChannel, PhoneServiceChannelId> {
  
}
