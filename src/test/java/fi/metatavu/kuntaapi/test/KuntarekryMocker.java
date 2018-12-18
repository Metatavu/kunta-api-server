package fi.metatavu.kuntaapi.test;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import fi.metatavu.kuntaapi.server.integrations.kuntarekry.KuntaRekryJob;

public class KuntarekryMocker extends AbstractMocker {

  public KuntarekryMocker mockKuntaRekryFeed(String path, String file) {
    List<KuntaRekryJob> jobs = readXMLFile(file, new TypeReference<List<KuntaRekryJob>>() { });
    mockGetXML(path, jobs, null);
    return this;
  }
  
}
