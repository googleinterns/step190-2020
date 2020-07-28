package com.google.sps.integration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

// Note: Integration tests must conform to the following naming pattern:
//   "**/IT*.java"
//   "**/*IT.java"
//   "**/*ITCase.java"
public class ExampleIT {

  private WebDriver driver;

  @Before
  public void setUp() {
    System.setProperty("webdriver.chrome.driver", "resources/chromedriver");
    driver = new ChromeDriver();
    driver.get("http://localhost:9876");
    System.out.println(driver.getTitle());
  }

  @Test
  public void testEndToEnd() throws Exception {
    // We should probably pass in the port number into the test, but IMO it's more trouble than it's
    // worth right now.
    URL url = new URL("http://localhost:9876");
    StringBuilder response = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine).append('\n');
      }
    }
    System.out.println(response.toString());
  }
}
