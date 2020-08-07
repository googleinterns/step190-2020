package com.google.sps.unit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.sps.servlets.ServletUtils;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ServletUtilsTest {
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

  // Test ServletUtils getRequestParam() function gets parameter in Optional container correctly.
  @Test
  public void getRequestParam_validParameterName_returnParameterValue() throws Exception {
    when(httpServletRequest.getParameter("myKey")).thenReturn("myValue");

    Optional<String> parameter =
        ServletUtils.getRequestParam(httpServletRequest, httpServletResponse, "myKey");
    Assert.assertTrue(parameter.isPresent());
    Assert.assertEquals(parameter.get(), "myValue");
  }

  // Test ServletUtils getRequestParam() function correctly returns empty Optional container when
  // parameter key does not exist.
  @Test
  public void getRequestParam_invalidParameterName_returnEmpty() throws Exception {
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    Optional<String> parameter =
        ServletUtils.getRequestParam(httpServletRequest, httpServletResponse, "anInvalidKey");
    Assert.assertFalse(parameter.isPresent());
    verify(printWriter).println("No anInvalidKey in the query URL.");
  }

  // Test ServletUtils getRequestParam() function correctly returns empty Optional container when
  // provided parameter key is null.
  @Test
  public void getRequestParam_nullParameterName_returnEmpty() throws Exception {
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    Optional<String> parameter =
        ServletUtils.getRequestParam(httpServletRequest, httpServletResponse, /*inputName=*/ null);
    Assert.assertFalse(parameter.isPresent());
    verify(printWriter).println("No null in the query URL.");
  }

  // Test finding an Election Entity in Datastore.
  @Test
  public void findElectionInDatastore_electionExists_returnCorrespondingEntity() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity electionEntity = new Entity("Election");
    electionEntity.setProperty("id", "9999");
    electionEntity.setProperty("name", "myElection");
    electionEntity.setProperty("scope", "myScope");
    electionEntity.setProperty("date", "myDate");
    electionEntity.setProperty("contests", new HashSet<Long>());
    electionEntity.setProperty("referendums", new HashSet<Long>());
    ds.put(electionEntity);

    Optional<Entity> foundEntity = ServletUtils.findElectionInDatastore(ds, "9999");
    Assert.assertTrue(foundEntity.isPresent());
    Assert.assertEquals(foundEntity.get(), electionEntity);
  }

  // Test finding an Election Entity that does not exist in Datastore.
  @Test
  public void findElectionInDatastore_electionDoesntExist_returnEmpty() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity electionEntity = new Entity("Election");
    electionEntity.setProperty("id", "9999");
    electionEntity.setProperty("name", "myElection");
    electionEntity.setProperty("scope", "myScope");
    electionEntity.setProperty("date", "myDate");
    electionEntity.setProperty("contests", new HashSet<Long>());
    electionEntity.setProperty("referendums", new HashSet<Long>());
    ds.put(electionEntity);

    Optional<Entity> foundEntity = ServletUtils.findElectionInDatastore(ds, "0001");
    Assert.assertFalse(foundEntity.isPresent());
  }
}
