package fi.metatavu.kuntaapi.test.server.integration.ptv;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.integrations.ptv.translation.PtvAddressSubtype;
import fi.metatavu.kuntaapi.server.persistence.model.clients.AccessType;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceChannelAttachment;
import fi.metatavu.kuntaapi.test.AbstractPtvMocker;
import fi.metatavu.ptv.client.model.V9VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPrintableFormChannelInBase;
import fi.metatavu.ptv.client.model.VmOpenApiAttachment;
import fi.metatavu.ptv.client.model.VmOpenApiLanguageItem;

@SuppressWarnings ("squid:S1075")
public class PrintableFormServiceChannelInTestsIT extends AbstractPtvInTest {

  private static final String SERVICE_CHANNEL_FIND_PATH = "/printableFormServiceChannels/{kuntaApiChannelId}";
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(getWireMockPort()), false);
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker().mock(TestPtvConsts.ORGANIZATIONS);
    getPtvServiceMocker().mock(TestPtvConsts.SERVICES);    
    getPtvServiceChannelMocker().mock(TestPtvConsts.SERVICE_CHANNELS);

    startMocks();
    
    waitApiListCount("/organizations", 3);
  }
  
  @Test
  public void updatePrintableFormServiceChannelUnauthorized() throws IOException, InterruptedException {
    PrintableFormServiceChannel kuntaApiResource = getPrintableFormServiceChannel(0, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    
    givenReadonly()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH, kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(401);
  }
  
  @Test
  public void updatePrintableFormServiceChannelForbidden() throws IOException, InterruptedException {
    PrintableFormServiceChannel kuntaApiResource = getPrintableFormServiceChannel(0, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH, kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(403);
  }
  
  @Test
  public void updatePrintableFormServiceChannelUnchanged() throws IOException, InterruptedException {
    String ptvId = TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS[0];
    String organizationId = getOrganizationId(0);
    String kuntaApiChannelId = getPrintableFormChannelId(0, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS);

    PrintableFormServiceChannel kuntaApiResource = getPrintableFormServiceChannel(0, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    V9VmOpenApiPrintableFormChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V9VmOpenApiPrintableFormChannelInBase.class);
    V9VmOpenApiPrintableFormChannel ptvOutResource =  getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V9VmOpenApiPrintableFormChannel.class);
    
    getPtvServiceChannelMocker().mockPrintableFormPut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH,kuntaApiChannelId)
      .then()
      .assertThat()
      .statusCode(200);

    getPtvServiceChannelMocker().verifyPrintableForm(ptvId, ptvInResource);
  }
  
  @Test
  public void updatePrintableFormServiceChannelChanges() throws IOException, InterruptedException {
    String ptvId = TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS[0];
    String organizationId = getOrganizationId(0);
    
    String kuntaApiChannelId = getPrintableFormChannelId(0, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS);

    PrintableFormServiceChannel kuntaApiResource = getPrintableFormServiceChannel(0, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    
    kuntaApiResource.setAreas(Arrays.asList(createArea("Municipality", "12345", createLocalizedValue("en", "Examplia"))));
    kuntaApiResource.setAreaType("AreaType");
    kuntaApiResource.setAttachments(createAttachments("en", "PDF", "https://www.example.com", "Example PDF", "PDF file for testing"));
    kuntaApiResource.setChannelUrls(createLocalizedValue("en", "URL", "https://www.example.com/channelurl"));
    kuntaApiResource.setDeliveryAddress(createAddressFreeText(createLocalizedValue("en", "Far far away")));
    kuntaApiResource.setDescriptions(createLocalizedValue("en", "Description", "Changed Description"));
    kuntaApiResource.setFormIdentifier(createLocalizedValue("en", "Example form 1234"));
    kuntaApiResource.setFormReceiver(createLocalizedValue("en", "Example receiver"));
    kuntaApiResource.setNames(createLocalizedValue("en", "Name", "Changed Name"));
    kuntaApiResource.setSupportEmails(createEmails("en", "fake@example.com"));
    kuntaApiResource.setSupportPhones(Arrays.asList(
      createPhone("en", "Phone", "+358", "12345-FAKE", "Charged", "Testing", false, "Test phone"),
      createPhone("en", "Fax", "+258", "54321-FAKE", "Free", "Testing fax", true, "Test fax")
    ));
    
    V9VmOpenApiPrintableFormChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V9VmOpenApiPrintableFormChannelInBase.class);
    ptvInResource.setAreas(Arrays.asList(createArea("Municipality", "12345")));
    ptvInResource.setAreaType("AreaType");
    ptvInResource.setAttachments(createPtvInAttachments("en", "https://www.example.com", "Example PDF", "PDF file for testing"));
    ptvInResource.setChannelUrls(createPtvInLocalizedItems("en", "URL", "https://www.example.com/channelurl"));
    
    List<VmOpenApiLanguageItem> formReceiver = createPtvInLanguageItems("en", "Example receiver");
    ptvInResource.setDeliveryAddresses(Arrays.asList(createPtvInDeliveryAddress(PtvAddressSubtype.NO_ADDRESS.getPtvValue(), null, null, createPtvInLanguageItems("en", "Far far away"), formReceiver)));
    ptvInResource.setServiceChannelDescriptions(createPtvInLocalizedItems("en", "Description", "Changed Description"));
    ptvInResource.setFormIdentifier(createPtvInLanguageItems("en", "Example form 1234"));
    ptvInResource.setServiceChannelNames(createPtvInLanguageItems("en", "Changed Name"));
    ptvInResource.setSupportEmails(createPtvInLanguageItems("en", "fake@example.com"));
    ptvInResource.setSupportPhones(createPtvInPhones("en", "+358", "12345-FAKE", "Charged", "Testing", false, "Test phone"));
    
    V9VmOpenApiPrintableFormChannel ptvOutResource =  getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V9VmOpenApiPrintableFormChannel.class);
    
    getPtvServiceChannelMocker().mockPrintableFormPut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH,kuntaApiChannelId)
      .then()
      .assertThat()
      .statusCode(200);

    getPtvServiceChannelMocker().verifyPrintableForm(ptvId, ptvInResource);
  }

  protected List<VmOpenApiAttachment> createPtvInAttachments(String language, String url, String name, String description) {
    VmOpenApiAttachment result = new VmOpenApiAttachment();
    result.setDescription(description);
    result.setLanguage(language);
    result.setName(name);
    result.setUrl(url);
    return Arrays.asList(result);
  }

  protected List<ServiceChannelAttachment> createAttachments(String language, String type, String url, String name, String description) {
    ServiceChannelAttachment result = new ServiceChannelAttachment();
    result.setDescription(description);
    result.setLanguage(language);
    result.setName(name);
    result.setType(type);
    result.setUrl(url);
    return Arrays.asList(result);
  }

}
