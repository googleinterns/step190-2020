package com.google.sps.integration;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

public class ElectionListIT {
  private WebDriver driver;

  @BeforeClass
  public static void setUp() {
    System.setProperty("webdriver.chrome.driver", "resources/chromedriver");
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

  @After
  public void tearDown() {
    driver.quit();
  }
}
