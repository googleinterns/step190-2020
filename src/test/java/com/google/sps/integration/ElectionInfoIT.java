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

public class ElectionInfoIT {
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
   * Tests if the error screen shows when the URL does not contain an election ID query parameter.
   * Tests that address form is not displayed.
   */
  @Test
  public void noElectionIdInQueryUrl_displayErrorText() {
    driver.get("http://localhost:9876/electionInfo.html");
    WebElement contentElement = driver.findElement(By.id("election-name-template"));
    WebElement paragraphElement = contentElement.findElement(By.xpath("//p[@id='title-text']"));
    Assert.assertEquals("Error! No election selected.", paragraphElement.getText());

    WebElement wrapperElement = driver.findElement(By.id("election-info-wrapper"));
    Assert.assertEquals("none", wrapperElement.getCssValue("display"));
  }

  /**
   * Tests if the regular election info screen shows when the URL does contain an election ID query
   * parameter. Tests that address form does display.
   */
  @Test
  public void electionIdInQueryUrl_displayRegularInfoCardScreen() {
    driver.get("http://localhost:9876/electionInfo.html?state=ca&electionId=2000");
    WebElement contentElement = driver.findElement(By.id("election-name-template"));
    WebElement paragraphElement = contentElement.findElement(By.xpath("//p[@id='title-text']/b"));
    Assert.assertEquals(
        "To show you polling locations and ballot items from the VIP Test Election, "
            + "we\'ll need your registered voter address:",
        paragraphElement.getText());

    WebElement wrapperElement = driver.findElement(By.id("election-info-wrapper"));
    Assert.assertEquals("block", wrapperElement.getCssValue("display"));
  }

  /**
   * TODO(anooshree): Resolve issue of being unable to test address input box locally due to API
   * restrictions Tests if the submit button allows submission when the site is first loaded, but
   * the user has not entered an address
   */
  @Test
  public void addressSubmission_onClick_blocksSubmissionWithoutAddress() {
    driver.get("http://localhost:9876/electionInfo.html?state=ca&electionId=2000");
    WebElement submitButton = driver.findElement(By.id("submit-address-button"));
    submitButton.click();

    Assert.assertEquals(
        "http://localhost:9876/electionInfo.html?state=ca&electionId=2000", driver.getCurrentUrl());
  }

  /** Test sending an invalid address to the API and getting the response. */
  @Test
  public void invalidAddressSubmission_onClick_displayErrorMessage() throws InterruptedException {
    driver.get("http://localhost:9876/electionInfo.html?state=ca&electionId=2000");
    driver.findElement(By.id("street_number")).sendKeys("x");
    driver.findElement(By.id("route")).sendKeys("x");
    driver.findElement(By.id("locality")).sendKeys("x");
    driver.findElement(By.id("administrative_area_level_1")).sendKeys("x");
    driver.findElement(By.id("country")).sendKeys("x");
    driver.findElement(By.id("postal_code")).sendKeys("x");

    WebElement submitButton = driver.findElement(By.id("submit-address-button"));
    Thread.sleep(1000);
    submitButton.click();

    Thread.sleep(10000);

    WebElement wrapperElement = driver.findElement(By.id("election-info-results"));
    Assert.assertEquals("", wrapperElement.getAttribute("innerHTML"));
    WebElement errorTextElement = driver.findElement(By.id("address-error-text"));
    Assert.assertEquals("block", errorTextElement.getCssValue("display"));
  }

  /** Test sending an invalid address to the API and getting the response. */
  @Test
  public void validAddressSubmission_onClick_hideErrorMessage() throws InterruptedException {
    driver.get("http://localhost:9876/electionInfo.html?state=ca&electionId=2000");
    driver.findElement(By.id("street_number")).sendKeys("1261");
    driver.findElement(By.id("route")).sendKeys("West 79th Street");
    driver.findElement(By.id("locality")).sendKeys("Los Angeles");
    driver.findElement(By.id("administrative_area_level_1")).sendKeys("CA");
    driver.findElement(By.id("country")).sendKeys("United States");
    driver.findElement(By.id("postal_code")).sendKeys("90044");

    WebElement submitButton = driver.findElement(By.id("submit-address-button"));
    Thread.sleep(1000);
    submitButton.click();

    Thread.sleep(10000);

    WebElement errorTextElement = driver.findElement(By.id("address-error-text"));
    Assert.assertEquals("none", errorTextElement.getCssValue("display"));
  }

  // TODO(anooshree): Check that Query URL updates after address submission

  // TODO(anooshree): Check that content loads for a valid URL

  @After
  public void tearDown() {
    driver.quit();
  }
}
