package fi.metatavu.kuntaapi.test.server.integration.ptv;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.skyscreamer.jsonassert.Customization;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;

import fi.metatavu.kuntaapi.test.AbstractIntegrationTest;
import fi.metatavu.kuntaapi.test.json.JSONAssertCustomizations;

/**
 * Abstract base class for all PTV tests
 * 
 * @author Antti Leppä
 */
public abstract class AbstractPtvTest extends AbstractIntegrationTest {
  
  /**
   * Waits that all organization services have been discovered
   *  
   * @param organizationIndex organization index
   */
  protected void waitOrganizationServices(int organizationIndex) {
    String organizationId = getOrganizationId(organizationIndex);
    
    await().atMost(5, TimeUnit.MINUTES).until(() -> givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}", organizationId)
      .body().jsonPath().getInt("services.size()") == TestPtvConsts.ORGANIZATION_SERVICES[organizationIndex].length
    );
  }
  
  /**
   * Waits that all service organizations have been discovered
   *  
   * @param serviceIndex organization index
   * @throws InterruptedException 
   */
  protected void waitServiceOrganizations(int serviceIndex, int organizationCount) throws InterruptedException {
    String serviceId = getServiceId(serviceIndex, TestPtvConsts.SERVICES.length);
    
    await().atMost(5, TimeUnit.MINUTES).until(() -> givenReadonly()
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}", serviceId)
      .body().jsonPath().getInt("organizations.size()") == organizationCount
    );
  }

  /**
   * Waits that all service channels have been discovered
   *  
   * @param serviceIndex service index
   * @throws InterruptedException when waiting is interrupted
   */
  protected void waitServiceChannels(final int serviceIndex) throws InterruptedException {
    String serviceId = getServiceId(serviceIndex, TestPtvConsts.SERVICES.length);
    
    await().atMost(5, TimeUnit.MINUTES).until(() -> {
      JsonPath jsonPath = givenReadonly()
        .contentType(ContentType.JSON)
        .get("/services/{serviceId}", serviceId)
        .body().jsonPath();
      
      return 
        jsonPath.getInt("electronicServiceChannelIds.size()") == TestPtvConsts.SERVICE_ELECTRONIC_CHANNEL_CHANNELS[serviceIndex].length &&
        jsonPath.getInt("phoneServiceChannelIds.size()") == TestPtvConsts.SERVICE_PHONE_CHANNELS[serviceIndex].length &&
        jsonPath.getInt("printableFormServiceChannelIds.size()") == TestPtvConsts.SERVICE_PRINTABLE_FORM_CHANNELS[serviceIndex].length &&
        jsonPath.getInt("serviceLocationServiceChannelIds.size()") == TestPtvConsts.SERVICE_SERVICE_LOCATION_CHANNELS[serviceIndex].length &&
        jsonPath.getInt("webPageServiceChannelIds.size()") == TestPtvConsts.SERVICE_WEB_PAGE_CHANNELS[serviceIndex].length;
    });
  }
  
  /**
   * Returns list of JSONAssert customizations for checking services.
   * 
   * @param serviceIndex service index
   * @return list of JSONAssert customizations for checking services.
   */
  protected Customization[] getServiceCustomizations(int serviceIndex) {
    return new Customization[] {
      JSONAssertCustomizations.notNull("id"),
      JSONAssertCustomizations.equalLength("electronicServiceChannelIds", TestPtvConsts.SERVICE_ELECTRONIC_CHANNEL_CHANNELS[serviceIndex].length),
      JSONAssertCustomizations.equalLength("phoneServiceChannelIds", TestPtvConsts.SERVICE_PHONE_CHANNELS[serviceIndex].length),
      JSONAssertCustomizations.equalLength("printableFormServiceChannelIds", TestPtvConsts.SERVICE_PRINTABLE_FORM_CHANNELS[serviceIndex].length),
      JSONAssertCustomizations.equalLength("serviceLocationServiceChannelIds", TestPtvConsts.SERVICE_SERVICE_LOCATION_CHANNELS[serviceIndex].length),
      JSONAssertCustomizations.equalLength("webPageServiceChannelIds", TestPtvConsts.SERVICE_WEB_PAGE_CHANNELS[serviceIndex].length)
    };
  }

  /**
   * Returns list of JSONAssert customizations for checking organizations.
   * 
   * @return list of JSONAssert customizations for checking organizations.
   */
  protected Customization[] getOrganizationCustomizations() {
    return JSONAssertCustomizations.notNulls("id", "services.serviceId", "services.organizationId");
  }
  

  /**
   * Returns list of JSONAssert customizations for checking service channel.
   * 
   * @return list of JSONAssert customizations for checking service channel.
   */
  protected Customization[] getServiceChannelCustomizations() {
    return JSONAssertCustomizations.notNulls("id");
  }
  
}
