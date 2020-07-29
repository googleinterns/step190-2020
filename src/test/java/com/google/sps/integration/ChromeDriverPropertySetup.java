package com.google.sps.integration;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/** Sets the "webdriver.chrome.driver" property to use the correct chromedriver binary depending on the os. */
public class ChromeDriverPropertySetup implements TestRule {
  @Override
  public Statement apply(Statement statement, Description description) {
    System.out.println(System.getProperty("os.name").toLowerCase());
    if (System.getProperty("os.name").toLowerCase().contains("mac")) {
      System.setProperty("webdriver.chrome.driver", "resources/chromedriver_mac");
    } else {
      System.setProperty("webdriver.chrome.driver", "resources/chromedriver");
    }
    try {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          statement.evaluate();
        }
      };
    } finally {

    }
  }
}
