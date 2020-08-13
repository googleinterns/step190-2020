package com.google.sps.servlets;

import com.google.appengine.api.datastore.Entity;
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

  // Test taking the JSON from the API response and creating a new PollingStation object
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
    Assert.assertEquals(pollingStation.getAddress(), "1 2 3, city, state zip");
    Assert.assertEquals(pollingStation.getPollingHours(), "-");
    Assert.assertEquals(pollingStation.getStartDate(), "start");
    Assert.assertEquals(pollingStation.getEndDate(), "end");
    Assert.assertEquals(pollingStation.getLocationType(), "pollingLocation");
  }

  @Test
  public void line1FieldMissing_testFromJsonObject() {
    JSONObject pollingStationJsonObject =
        new JSONObject(
            "{\"id\": \"pollingId\","
                + "\"address\": {\"locationName\": \"name\","
                + "\"line2\": \"2\",\"line3\": \"3\",\"city\": \"city\",\"state\": \"state\","
                + "\"zip\": \"zip\"},\"pollingHours\": \"-\",\"name\": \"pollingStation\","
                + "\"startDate\": \"start\",\"endDate\": \"end\"}");

    PollingStation pollingStation =
        PollingStation.fromJSONObject(pollingStationJsonObject, "pollingLocation");

    Assert.assertEquals(pollingStation.getName(), "pollingStation");
    Assert.assertEquals(pollingStation.getAddress(), "2 3, city, state zip");
    Assert.assertEquals(pollingStation.getPollingHours(), "-");
    Assert.assertEquals(pollingStation.getStartDate(), "start");
    Assert.assertEquals(pollingStation.getEndDate(), "end");
    Assert.assertEquals(pollingStation.getLocationType(), "pollingLocation");
  }

  @Test
  public void line2FieldMissing_testFromJsonObject() {
    JSONObject pollingStationJsonObject =
        new JSONObject(
            "{\"id\": \"pollingId\","
                + "\"address\": {\"locationName\": \"name\","
                + "\"line1\": \"1\",\"line3\": \"3\",\"city\": \"city\",\"state\": \"state\","
                + "\"zip\": \"zip\"},\"pollingHours\": \"-\",\"name\": \"pollingStation\","
                + "\"startDate\": \"start\",\"endDate\": \"end\"}");

    PollingStation pollingStation =
        PollingStation.fromJSONObject(pollingStationJsonObject, "pollingLocation");

    Assert.assertEquals(pollingStation.getName(), "pollingStation");
    Assert.assertEquals(pollingStation.getAddress(), "1 3, city, state zip");
    Assert.assertEquals(pollingStation.getPollingHours(), "-");
    Assert.assertEquals(pollingStation.getStartDate(), "start");
    Assert.assertEquals(pollingStation.getEndDate(), "end");
    Assert.assertEquals(pollingStation.getLocationType(), "pollingLocation");
  }

  @Test
  public void line3FieldMissing_testFromJsonObject() {
    JSONObject pollingStationJsonObject =
        new JSONObject(
            "{\"id\": \"pollingId\","
                + "\"address\": {\"locationName\": \"name\","
                + "\"line1\": \"1\",\"line2\": \"2\",\"city\": \"city\",\"state\": \"state\","
                + "\"zip\": \"zip\"},\"pollingHours\": \"-\",\"name\": \"pollingStation\","
                + "\"startDate\": \"start\",\"endDate\": \"end\"}");

    PollingStation pollingStation =
        PollingStation.fromJSONObject(pollingStationJsonObject, "pollingLocation");

    Assert.assertEquals(pollingStation.getName(), "pollingStation");
    Assert.assertEquals(pollingStation.getAddress(), "1 2, city, state zip");
    Assert.assertEquals(pollingStation.getPollingHours(), "-");
    Assert.assertEquals(pollingStation.getStartDate(), "start");
    Assert.assertEquals(pollingStation.getEndDate(), "end");
    Assert.assertEquals(pollingStation.getLocationType(), "pollingLocation");
  }

  @Test
  public void zipCodeFieldMissing_testFromJsonObject() {
    JSONObject pollingStationJsonObject =
        new JSONObject(
            "{\"id\": \"pollingId\","
                + "\"address\": {\"locationName\": \"name\",\"line1\": \"1\","
                + "\"line2\": \"2\",\"line3\": \"3\",\"city\": \"city\",\"state\": \"state\"},"
                + "\"pollingHours\": \"-\",\"name\": \"pollingStation\","
                + "\"startDate\": \"start\",\"endDate\": \"end\"}");

    PollingStation pollingStation =
        PollingStation.fromJSONObject(pollingStationJsonObject, "pollingLocation");

    Assert.assertEquals(pollingStation.getName(), "pollingStation");
    Assert.assertEquals(pollingStation.getAddress(), "1 2 3, city, state ");
    Assert.assertEquals(pollingStation.getPollingHours(), "-");
    Assert.assertEquals(pollingStation.getStartDate(), "start");
    Assert.assertEquals(pollingStation.getEndDate(), "end");
    Assert.assertEquals(pollingStation.getLocationType(), "pollingLocation");
  }

  @Test
  public void nameFieldMissing_testFromJsonObject() {
    JSONObject pollingStationJsonObject =
        new JSONObject(
            "{\"id\": \"pollingId\","
                + "\"address\": {\"locationName\": \"name\",\"line1\": \"1\","
                + "\"line2\": \"2\",\"line3\": \"3\",\"city\": \"city\",\"state\": \"state\","
                + "\"zip\": \"zip\"},\"pollingHours\": \"-\","
                + "\"startDate\": \"start\",\"endDate\": \"end\"}");

    PollingStation pollingStation =
        PollingStation.fromJSONObject(pollingStationJsonObject, "pollingLocation");

    Assert.assertEquals(pollingStation.getName(), "Polling Station");
    Assert.assertEquals(pollingStation.getAddress(), "1 2 3, city, state zip");
    Assert.assertEquals(pollingStation.getPollingHours(), "-");
    Assert.assertEquals(pollingStation.getStartDate(), "start");
    Assert.assertEquals(pollingStation.getEndDate(), "end");
    Assert.assertEquals(pollingStation.getLocationType(), "pollingLocation");
  }

  @Test
  public void pollingHoursFieldMissing_testFromJsonObject() {
    JSONObject pollingStationJsonObject =
        new JSONObject(
            "{\"id\": \"pollingId\","
                + "\"address\": {\"locationName\": \"name\",\"line1\": \"1\","
                + "\"line2\": \"2\",\"line3\": \"3\",\"city\": \"city\",\"state\": \"state\","
                + "\"zip\": \"zip\"},\"name\": \"pollingStation\","
                + "\"startDate\": \"start\",\"endDate\": \"end\"}");

    PollingStation pollingStation =
        PollingStation.fromJSONObject(pollingStationJsonObject, "pollingLocation");

    Assert.assertEquals(pollingStation.getName(), "pollingStation");
    Assert.assertEquals(pollingStation.getAddress(), "1 2 3, city, state zip");
    Assert.assertEquals(pollingStation.getPollingHours(), "daily");
    Assert.assertEquals(pollingStation.getStartDate(), "start");
    Assert.assertEquals(pollingStation.getEndDate(), "end");
    Assert.assertEquals(pollingStation.getLocationType(), "pollingLocation");
  }

  @Test
  public void startDateFieldMissing_testFromJsonObject() {
    JSONObject pollingStationJsonObject =
        new JSONObject(
            "{\"id\": \"pollingId\","
                + "\"address\": {\"locationName\": \"name\",\"line1\": \"1\","
                + "\"line2\": \"2\",\"line3\": \"3\",\"city\": \"city\",\"state\": \"state\","
                + "\"zip\": \"zip\"},\"pollingHours\": \"-\",\"name\": \"pollingStation\","
                + "\"endDate\": \"end\"}");

    PollingStation pollingStation =
        PollingStation.fromJSONObject(pollingStationJsonObject, "pollingLocation");

    Assert.assertEquals(pollingStation.getName(), "pollingStation");
    Assert.assertEquals(pollingStation.getAddress(), "1 2 3, city, state zip");
    Assert.assertEquals(pollingStation.getPollingHours(), "-");
    Assert.assertEquals(pollingStation.getStartDate(), "on an unknown start date");
    Assert.assertEquals(pollingStation.getEndDate(), "end");
    Assert.assertEquals(pollingStation.getLocationType(), "pollingLocation");
  }

  @Test
  public void endDateFieldMissing_testFromJsonObject() {
    JSONObject pollingStationJsonObject =
        new JSONObject(
            "{\"id\": \"pollingId\","
                + "\"address\": {\"locationName\": \"name\",\"line1\": \"1\","
                + "\"line2\": \"2\",\"line3\": \"3\",\"city\": \"city\",\"state\": \"state\","
                + "\"zip\": \"zip\"},\"pollingHours\": \"-\",\"name\": \"pollingStation\","
                + "\"startDate\": \"start\"}");

    PollingStation pollingStation =
        PollingStation.fromJSONObject(pollingStationJsonObject, "pollingLocation");

    Assert.assertEquals(pollingStation.getName(), "pollingStation");
    Assert.assertEquals(pollingStation.getAddress(), "1 2 3, city, state zip");
    Assert.assertEquals(pollingStation.getPollingHours(), "-");
    Assert.assertEquals(pollingStation.getStartDate(), "start");
    Assert.assertEquals(pollingStation.getEndDate(), "an unknown end date");
    Assert.assertEquals(pollingStation.getLocationType(), "pollingLocation");
  }

  // Test creating a new PollingStation object from an Entity in Datastore
  @Test
  public void testFromEntity() throws Exception {
    Entity entity = new Entity("PollingStation");
    entity.setProperty("name", "name");
    entity.setProperty("address", "address");
    entity.setProperty("pollingHours", "-");
    entity.setProperty("startDate", "start");
    entity.setProperty("endDate", "end");
    entity.setProperty("locationType", "dropOffLocation");

    PollingStation pollingStation = PollingStation.fromEntity(entity);

    Assert.assertEquals(pollingStation.getName(), "name");
    Assert.assertEquals(pollingStation.getAddress(), "address");
    Assert.assertEquals(pollingStation.getPollingHours(), "-");
    Assert.assertEquals(pollingStation.getStartDate(), "start");
    Assert.assertEquals(pollingStation.getEndDate(), "end");
    Assert.assertEquals(pollingStation.getLocationType(), "dropOffLocation");
  }

  // Test creating a new Entity in Datastore from a PollingLocation object
  @Test
  public void testToEntity() throws Exception {
    PollingStation pollingStation =
        PollingStation.builder()
            .setName("name")
            .setAddress("address")
            .setPollingHours("-")
            .setStartDate("start")
            .setEndDate("end")
            .setLocationType("earlyVoteSite")
            .build();

    Entity pollingStationEntity = pollingStation.toEntity();

    Assert.assertEquals(pollingStationEntity.getProperty("name"), "name");
    Assert.assertEquals(pollingStationEntity.getProperty("address"), "address");
    Assert.assertEquals(pollingStationEntity.getProperty("pollingHours"), "-");
    Assert.assertEquals(pollingStationEntity.getProperty("startDate"), "start");
    Assert.assertEquals(pollingStationEntity.getProperty("endDate"), "end");
    Assert.assertEquals(pollingStationEntity.getProperty("locationType"), "earlyVoteSite");
  }
}
