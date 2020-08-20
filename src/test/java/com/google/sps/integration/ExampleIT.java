package com.google.sps.integration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

// Note: Integration tests must conform to the following naming pattern:
//   "**/IT*.java"
//   "**/*IT.java"
//   "**/*ITCase.java"
public class ExampleIT {
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

  @After
  public void tearDown() {
    driver.quit();
  }

  @Test
  public void testEndToEnd() throws Exception {
    // We should probably pass in the port number into the test, but IMO it's more trouble than it's
    // worth right now.
    String response = getUrlResponse("http://localhost:9876/election");
    System.out.println(response);
    Assert.assertTrue(response.contains("Oklahoma Primary Runoff Election and Special Elections"));
  }

  private String getUrlResponse(String urlAddress) throws Exception {
    URL url = new URL(urlAddress);
    StringBuilder response = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine).append('\n');
      }
    }
    return response.toString();
  }
}
