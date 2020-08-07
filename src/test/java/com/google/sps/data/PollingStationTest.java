package com.google.sps.servlets;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.sps.data.PollingStation;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PollingStationTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalURLFetchServiceTestConfig());

  @Mock HttpServletRequest httpServletRequest;
  @Mock HttpServletResponse httpServletResponse;
  @Mock PrintWriter printWriter;

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testFromJSONObject() throws Exception {
    JSONObject pollingStationJsonObject =
        new JSONObject(
            "{\"id\": \"pollingId\","
                + "\"address\": {\"locationName\": \"name\",\"line1\": \"1\","
                + "\"line2\": \"2\",\"line3\": \"3\",\"city\": \"city\",\"state\": \"state\","
                + "\"zip\": \"zip\"},\"pollingHours\": \"-\",\"name\": \"pollingStation\","
                + "\"startDate\": \"start\",\"endDate\": \"end\"}");

    PollingStation pollingStation =
        PollingStation.fromJSONObject(pollingStationJsonObject, "pollingLocation");

    Assert.assertEquals(pollingStation.getName(), "pollingStation");
    Assert.assertEquals(pollingStation.getAddress(), "1 2 3 city state zip");
    Assert.assertEquals(pollingStation.getPollingHours(), "-");
    Assert.assertEquals(pollingStation.getStartDate(), "start");
    Assert.assertEquals(pollingStation.getEndDate(), "end");
    Assert.assertEquals(pollingStation.getLocationType(), "pollingLocation");
  }
}
