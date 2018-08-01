package fi.metatavu.kuntaapi.server.index.search;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.PhoneServiceChannelId;
import fi.metatavu.kuntaapi.server.index.IndexablePhoneServiceChannel;

@ApplicationScoped
public class PhoneServiceChannelSearcher extends AbstractServiceChannelSearcher<IndexablePhoneServiceChannel, PhoneServiceChannelId> {
  
}
