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

public class WelcomePageIT {
  private WebDriver driver;
  private final int WAIT_TIME = 30;
  private final int MAX_NUM_TABS = 2;

  @BeforeClass
  public static void setUp() {
    System.setProperty("webdriver.chrome.driver", "resources/chromedriver");
  }

  @Before
  public void testSetUp() {
    driver = new ChromeDriver();
  }

  /**
   * Testing if clicking on TurboVotes button opens a new tab and redirects the user to the
   * TurboVotes page.
   */
  @Test
  public void testTurboVotesButton() {
    driver.get("http://localhost:9876");

    // Ensure only one window is open.
    assert driver.getWindowHandles().size() == 1;

    String currentWindow = driver.getWindowHandle();
    WebElement turboVotesButton = driver.findElement(By.id("turbo-vote-button"));
    turboVotesButton.click();

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

  /** Testing if clicking "Show me my elections" button navigates to the elections list */
  @Test
  public void testElectionListButton() {
    driver.get("http://localhost:9876");

    // Ensure only one window is open.
    assert driver.getWindowHandles().size() == 1;

    String currentWindow = driver.getWindowHandle();
    WebElement electionListButton = driver.findElement(By.id("show-elections-button"));
    electionListButton.click();

    Assert.assertEquals("http://localhost:9876/electionlist.html", driver.getCurrentUrl());
  }

  @After
  public void tearDown() {
    driver.quit();
  }
}
