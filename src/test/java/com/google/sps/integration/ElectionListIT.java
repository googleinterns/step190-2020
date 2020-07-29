package com.google.sps.integration;

import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

public class ElectionListIT {
  private WebDriver driver;

  @ClassRule
  public static ChromeDriverPropertySetup chromeDriverPropertySetup =
      new ChromeDriverPropertySetup();

  @Before
  public void testSetUp() {
    driver = new ChromeDriver();
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
