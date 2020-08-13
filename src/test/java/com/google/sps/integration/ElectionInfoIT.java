package com.google.sps.integration;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
  /**
   * Tests if the query URL updates with the user address following address submission using the
   * autocomplete feature
   */
  @Test
  public void addressSubmission_onClick_createAndUpdateQueryParams() throws InterruptedException {

    driver.get("http://localhost:9876/electionInfo.html?state=ca&electionId=2000");
    WebDriverWait wait = new WebDriverWait(driver, 20);

    WebElement autocompleteBox = driver.findElement(By.id("autocomplete"));
    autocompleteBox.sendKeys("121 Bernal Road, San Jose");
    Thread.sleep(5000);

    wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("autocomplete"))))
        .sendKeys(Keys.DOWN, Keys.ENTER);

    Thread.sleep(1000);

    WebElement submitButton = driver.findElement(By.id("submit-address-button"));
    submitButton.click();

    String targetAddressValue = "121+Bernal+Road+San+Jose+CA+95119+United+States";
    Assert.assertEquals(
        "http://localhost:9876/electionInfo.html?state=ca&electionId=2000&address="
            + targetAddressValue,
        driver.getCurrentUrl());
  }

  // TODO(anooshree): Check that content loads for a valid URL
  // @Test
  // public void addressSubmission_afterSubmission_contentLoads() {}

  @After
  public void tearDown() {
    driver.quit();
  }
}
