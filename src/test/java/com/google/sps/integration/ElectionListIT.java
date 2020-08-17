package com.google.sps.integration;

import static org.mockito.Mockito.when;

import com.google.sps.servlets.ElectionServlet;
import com.google.sps.servlets.ServletUtils;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServletUtils.class)
public class ElectionListIT {
  private WebDriver driver;

  @ClassRule
  public static ChromeDriverPropertySetup chromeDriverPropertySetup =
      new ChromeDriverPropertySetup();

  // @Mock ServletUtils servletUtils;
  @Mock HttpServletRequest httpServletRequest;
  @Mock HttpServletResponse httpServletResponse;

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
  // @Rule public PowerMockRule rule = new PowerMockRule();

  // @RunWith(PowerMockRunner.class)
  // @PrepareForTest(ServletUtils.class);

  @Before
  public void testSetUp() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--headless");
    options.setExperimentalOption("useAutomationExtension", false);
    driver = new ChromeDriver(options);
  }

  /**
   * Tests if selecting a state from the state selection dropdown changes the URL of the site to
   * have the query parameter 'state' with the value being the two letter state code of the selected
   * state.
   */
  @Test
  public void stateSelection_onSelection_createAndUpdateStateQueryParameter() {
    driver.get("http://localhost:9876/electionlist.html");
    WebElement stateSelectElement = driver.findElement(By.id("select-state"));
    Select stateSelect = new Select(stateSelectElement);

    String targetStateValue = "ca";
    stateSelect.selectByValue(targetStateValue);
    Assert.assertEquals(
        "http://localhost:9876/electionlist.html?state=" + targetStateValue,
        driver.getCurrentUrl());

    targetStateValue = "fl";
    stateSelect.selectByValue(targetStateValue);
    Assert.assertEquals(
        "http://localhost:9876/electionlist.html?state=" + targetStateValue,
        driver.getCurrentUrl());
  }

  /**
   * Tests if selecting an election from the list available post-state selection changes the URL to
   * redirect to the election information page and add the query parameters 'electionName' with the
   * value being the name of the selected election and 'electionID' with the value being its ID.
   */
  @Test
  public void electionSelection_onClick_redirectAndUpdateElectionQueryParameters()
      throws InterruptedException, IOException {

    String electionApiKey = ServletUtils.getApiKey("112408856470", "election-api-key", "1");
    String baseUrl = "https://www.googleapis.com/civicinfo/v2/elections?key=%s";

    PowerMockito.mockStatic(ServletUtils.class);
    when(ServletUtils.readFromApiUrl(String.format(baseUrl, electionApiKey)))
        .thenReturn(
            new JSONObject(
                "[{\"id\":\"5012\",\"name\":\"Wyoming State Primary Election\","
                    + "\"date\":\"2020-08-18\",\"scope\":\"ocd-division/country:us/state:wy\","
                    + "\"contests\":[],\"referendums\":[],\"pollingStations\":[]},{\"id\":\"2000\","
                    + "\"name\":\"VIP Test Election\",\"date\":\"2021-06-06\",\"scope\":\"ocd-division/country:us\","
                    + "\"contests\":[5044840450490368,4808305327210496,4650809144901632,5197556602634240,"
                    + "5348070778732544,6020922771243008,6288648114798592,6199998815404032,4898779845099520,"
                    + "4924084290846720,4865541068029952],\"referendums\":[6282131944767488,6049315952787456,"
                    + "5941728003489792],\"pollingStations\":[{\"key\":{\"kind\":\"PollingStation\","
                    + "\"id\":5976267149017088},\"propertyMap\":{\"address\":\"14500 LANDSTAR BLVD  , ORLANDO, FL 32824\","
                    + "\"endDate\":\"an unknown end date\",\"name\":\"Polling Station\",\"locationType\":\"pollingLocation\","
                    + "\"startDate\":\"on an unknown start date\",\"pollingHours\":\"7:00 AM - 7:00 PM\"}}]},{\"id\":\"5013\","
                    + "\"name\":\"Florida State Primary Election\",\"date\":\"2020-08-18\",\"scope\":\"ocd-division/country:us/state:fl\","
                    + "\"contests\":[],\"referendums\":[],\"pollingStations\":[]},{\"id\":\"4953\","
                    + "\"name\":\"Louisiana Municipal General Election\",\"date\":\"2020-08-15\","
                    + "\"scope\":\"ocd-division/country:us/state:la\",\"contests\":[],\"referendums\":[],"
                    + "\"pollingStations\":[]},{\"id\":\"5011\",\"name\":\"Alaska State Primary Election\","
                    + "\"date\":\"2020-08-18\",\"scope\":\"ocd-division/country:us/state:ak\",\"contests\":[],"
                    + "\"referendums\":[],\"pollingStations\":[]},{\"id\":\"5015\","
                    + "\"name\":\"Oklahoma Primary Runoff Election and Special Elections\",\"date\":\"2020-08-25\","
                    + "\"scope\":\"ocd-division/country:us/state:ok\",\"contests\":[],\"referendums\":[],\"pollingStations\":[]}]"));

    new ElectionServlet().doPut(httpServletRequest, httpServletResponse);

    driver.get("http://localhost:9876/electionlist.html?state=wy");

    WebElement learnMoreButton = driver.findElement(By.id("state-learn-more-button"));
    learnMoreButton.click();

    String targetElectionID = "5012";

    Thread.sleep(2000);

    Assert.assertEquals(
        String.format(
            "http://localhost:9876/electionInfo.html?state=ga&electionId=%s", targetElectionID),
        driver.getCurrentUrl());
  }

  @After
  public void tearDown() {
    driver.quit();
  }
}
