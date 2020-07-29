package com.google.sps.integration;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WelcomePageIT {
  private WebDriver driver;
  private final int WAIT_TIME = 30;
  private final int MAX_NUM_TABS = 2;

  @Before
  public void setUp() {
    driver = new HtmlUnitDriver();
  }

  /**
   * Testing if clicking on TurboVotes button opens a new tab and redirects the user to the
   * TurboVotes page.
   */
  @Test
  public void testTurboVotesButton() {
    driver.get("http://localhost:9876");
    assert driver.getWindowHandles().size() == 1;

    String currentWindow = driver.getWindowHandle();
    WebElement turboVotesButton = driver.findElement(By.id("turbo-vote-link"));
    turboVotesButton.click();
    new WebDriverWait(driver, WAIT_TIME);

    // Ensure a new tab is created.
    Assert.assertEquals(MAX_NUM_TABS, driver.getWindowHandles().size());

    // Find new tab/window and switch to it.
    for (String windowHandle : driver.getWindowHandles()) {
      if (!currentWindow.contentEquals(windowHandle)) {
        driver.switchTo().window(windowHandle);
        break;
      }
    }

    Assert.assertEquals("TurboVote", driver.getTitle());
  }

  @After
  public void tearDown() {
    driver.quit();
  }
}
