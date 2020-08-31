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
  public void noElectionIdInQueryUrl_displayErrorText() throws InterruptedException {
    driver.get("http://localhost:9876/electionInfo.html");
    Thread.sleep(5000);

    WebElement paragraphElement = driver.findElement(By.id("title-text"));
    Assert.assertEquals("Error! No election selected.", paragraphElement.getText());

    WebElement wrapperElement = driver.findElement(By.id("election-info-wrapper"));
    Assert.assertEquals("none", wrapperElement.getCssValue("display"));
  }

  /**
   * Tests if the regular election info screen shows when the URL does contain an election ID query
   * parameter. Tests that address form does display.
   */
  @Test
  public void electionIdInQueryUrl_displayRegularInfoCardScreen() throws InterruptedException {
    driver.get("http://localhost:9876/electionInfo.html?state=ca&electionId=2000");
    Thread.sleep(5000);
    WebElement paragraphElement = driver.findElement(By.id("title-text"));
    Assert.assertEquals(
        "To show you polling locations and ballot items from the VIP Test Election, "
            + "we\'ll need your registered voter address:",
        paragraphElement.getText());

    WebElement wrapperElement = driver.findElement(By.id("election-info-wrapper"));
    Assert.assertEquals("block", wrapperElement.getCssValue("display"));
  }

  /**
   * Tests if the submit button allows submission when the site is first loaded, but the user has
   * not entered an address
   */
  @Test
  public void addressSubmission_onClick_blocksSubmissionWithoutAddress() {
    driver.get("http://localhost:9876/electionInfo.html?state=ca&electionId=2000");
    WebElement submitButton = driver.findElement(By.id("submit-address-button"));
    submitButton.click();

    Assert.assertEquals(
        "http://localhost:9876/electionInfo.html?state=ca&electionId=2000", driver.getCurrentUrl());
  }

  /**
   * Tests if the deadlines appear upon page load, given that the user's state is present in the
   * query parameters.
   */
  @Test
  public void stateInQueryUrl_displayDatesAndDeadlinesOnScreen() throws InterruptedException {
    driver.get("http://localhost:9876/electionInfo.html?state=ca&electionId=2000");
    Thread.sleep(5000);

    WebElement contentElement = driver.findElement(By.id("deadlines-template"));
    WebElement paragraphElement =
        contentElement.findElement(By.xpath("//h3[@id='general-deadlines-title']/b"));
    Assert.assertEquals(
        "Key deadlines in California for this national election:", paragraphElement.getText());
  }

  @After
  public void tearDown() {
    driver.quit();
  }
}
