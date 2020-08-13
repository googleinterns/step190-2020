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

  // TODO(anooshree): Check that Query URL updates after address submission

  // TODO(anooshree): Check that content loads for a valid URL

  @After
  public void tearDown() {
    driver.quit();
  }
}
