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
   * Tests if the regular election info screen shows when the URL does contain an election ID query parameter.
   * Tests that address form does display.
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
   * restrictions
   */
  @After
  public void tearDown() {
    driver.quit();
  }
}
