package com.google.sps.integration;

import org.junit.Test;

// @RunWith(JUnit4.class)
public class ExampleIT {

  @Test
  public void testEndToEnd() throws Exception {
    for (int i = 0; i < 3; i++) {
      System.out.println("This is iteration=" + i);
      Thread.sleep(3 * 1000);
    }
    //        URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();
    //        HTTPResponse resp = fetchService.fetch(new URL("http://localhost:" +
    //                System.getProperty(DevAppServerTest.DEFAULT_PORT_SYSTEM_PROPERTY) +
    // "/insertFoo?id=33"));
    //        assertEquals(200, resp.getResponseCode());
    //        DatastoreServiceFactory.getDatastoreService().get(KeyFactory.createKey("foo", 33));
  }
}
