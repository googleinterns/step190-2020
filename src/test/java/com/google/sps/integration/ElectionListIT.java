package com.google.sps.integration;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

public class ElectionListIT {
  private WebDriver driver;

  @ClassRule
  public static ChromeDriverPropertySetup chromeDriverPropertySetup =
      new ChromeDriverPropertySetup();

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
      throws InterruptedException {
    driver.get("http://localhost:9876/electionlist.html?state=ga");

    WebElement learnMoreButton = driver.findElement(By.id("state-learn-more-button"));
    learnMoreButton.click();

    String targetElectionName = "Georgia+General+Primary+Runoff+Election";
    String targetElectionID = "4979";

    Thread.sleep(2000);

    Assert.assertEquals(
        String.format(
            "http://localhost:9876/electionInfo.html?state=ga&electionId=%s&electionName=%s",
            targetElectionID, targetElectionName),
        driver.getCurrentUrl());
  }

  @After
  public void tearDown() {
    driver.quit();
  }
}
