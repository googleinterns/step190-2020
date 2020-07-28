package com.google.sps.integration;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.Select;


public class ElectionListIT {

  @Before
  public void setUp() {
    driver = new HtmlUnitDriver(true);
    driver.get("http://localhost:9876");
  }

  @Test
  public void testEndToEnd() throws Exception {
 
  }
}
