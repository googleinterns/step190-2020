package com.google.sps.integration;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

public class ElectionListIT {
  private WebDriver driver;
  private static final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private static Entity getElectionEntity(
      String stateCode, String ElectionName, long id, String electionDate) {
    Entity newElection = new Entity("Election");
    newElection.setProperty("id", id);
    newElection.setProperty("name", ElectionName);
    if (!stateCode.equals("us")) {
      newElection.setProperty("scope", "ocd-division/country:us/state:" + stateCode);
    } else {
      newElection.setProperty("scope", "ocd-division/country:us");
    }
    newElection.setProperty("date", electionDate);
    return newElection;
  }

  @ClassRule
  public static ChromeDriverPropertySetup chromeDriverPropertySetup =
      new ChromeDriverPropertySetup();

  @BeforeClass
  public static void datastoreSetUp() {
    helper.setUp();
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    String currentState = "ca";
    Entity californiaElectionOne =
        getElectionEntity(currentState, "California Primary", 6000, "2020-10-01");
    Entity californiaElectionTwo =
        getElectionEntity(currentState, "California Senate Election", 6001, "2020-10-02");
    ds.put(californiaElectionOne);
    ds.put(californiaElectionTwo);

    currentState = "fl";
    Entity floridaElectionOne =
        getElectionEntity(currentState, "Florida Primary", 6002, "2020-10-03");
    ds.put(floridaElectionOne);

    Entity nationalElection = getElectionEntity("us", "Presidential Election", 6003, "2020-11-03");
    ds.put(nationalElection);
  }

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

  @Test
  public void stateSelection_onSelection_retrievesCorrectElections() {
    driver.get("http://localhost:9876/electionlist.html");
    WebElement stateSelectElement = driver.findElement(By.id("select-state"));
    Select stateSelect = new Select(stateSelectElement);

    String targetStateValue = "ca";
    stateSelect.selectByValue(targetStateValue);
  }

  @After
  public void testTearDown() {
    driver.quit();
  }

  @AfterClass
  public static void datastoreTearDown() {
    helper.tearDown();
  }
}
