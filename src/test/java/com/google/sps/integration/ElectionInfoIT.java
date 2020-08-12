package com.google.sps.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.openqa.selenium.WebDriver;
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
  // @Test
  // public void addressSubmission_onClick_blocksSubmissionWithoutAddress() {}

  // TODO(anooshree): Check that Query URL updates after address submission
  /**
   * Tests if the query URL updates with the user address following address submission using the
   * autocomplete feature
   */
  // @Test
  // public void addressSubmission_onClick_createAndUpdateQueryParams() {}

  // TODO(anooshree): Check that content loads for a valid URL
  // @Test
  // public void addressSubmission_afterSubmission_contentLoads() {}

  @After
  public void tearDown() {
    driver.quit();
  }
}
