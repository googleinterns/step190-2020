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

public class WelcomePageIT {
  private WebDriver driver;
  private static final int WAIT_TIME = 30;
  private static final int MAX_NUM_TABS = 2;

  @BeforeClass
  public static void setUp() {
    System.setProperty("webdriver.chrome.driver", "resources/chromedriver");
  }

  @Before
  public void testSetUp() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.setExperimentalOption("useAutomationExtension", false);
    driver = new ChromeDriver(options);
  }

  /**
   * Testing if clicking on TurboVotes button opens a new tab and redirects the user to the
   * TurboVotes page.
   */
  @Test
  public void turboVotesButton_onClick_redirectsToTurboVotes() {
    driver.get("http://localhost:9876");

    Assert.assertEquals(1, driver.getWindowHandles().size());

    WebElement turboVotesButton = driver.findElement(By.id("turbo-vote-button"));
    turboVotesButton.click();

    Assert.assertEquals(MAX_NUM_TABS, driver.getWindowHandles().size());

    String currentWindow = driver.getWindowHandle();
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
  public void electionListButton_onClick_navigatesToElectionListPage() {
    driver.get("http://localhost:9876");

    Assert.assertEquals(1, driver.getWindowHandles().size());

    WebElement electionListButton = driver.findElement(By.id("show-elections-button"));
    electionListButton.click();

    Assert.assertEquals("http://localhost:9876/electionlist.html", driver.getCurrentUrl());
  }

  @After
  public void tearDown() {
    driver.quit();
  }
}
