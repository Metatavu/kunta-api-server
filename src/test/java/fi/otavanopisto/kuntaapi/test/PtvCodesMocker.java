package fi.otavanopisto.kuntaapi.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.KuntaRekryJob;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;

public class PtvCodesMocker extends AbstractMocker {
  
  @Override
  public void startMock() {
    String[] areaCodeTypes = {"Province", "HospitalRegions", "BusinessRegions"};
    for (String areaCodeType : areaCodeTypes) {
      mockCodes(String.format("/CodeList/GetAreaCodes/type/%s", areaCodeType), String.format("%s.json", areaCodeType.toLowerCase()));
    }

    mockCodes("/CodeList/GetCountryCodes", "country.json");
    mockCodes("/CodeList/GetLanguageCodes", "language.json");
    mockCodes("/CodeList/GetMunicipalityCodes", "municipality.json");
    mockCodesPaged("/CodeList/GetPostalCodes", "postal.json", 0);
    
    super.startMock();
  }
  
  private void mockCodes(String path, String file) {
    mockGetString(String.format("/ptv/api/%s%s", PtvConsts.VERSION, path), "application/json", readFile(String.format("ptv/codes/%s", file)));
  }
  
  private void mockCodesPaged(String path, String file, int page) {
    Map<String, String> params = new HashMap<>();
    params.put("page", String.valueOf(page));
    mockGetString(String.format("/ptv/api/%s%s", PtvConsts.VERSION, path), "application/json", readFile(String.format("ptv/codes/%s", file)), params);
  }
  
  public PtvCodesMocker mockKuntaRekryFeed(String path, String file) {
    List<KuntaRekryJob> jobs = readXMLFile(file, new TypeReference<List<KuntaRekryJob>>() { });
    mockGetXML(path, jobs, null);
    return this;
  }
  
}
