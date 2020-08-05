package com.google.sps.unit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.sps.servlets.ServletUtils;
import java.io.PrintWriter;
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

  // Test ServletUtils getRquestParam() function gets parameter in Optional container correctly.
  @Test
  public void testHTTPRequestGetValidParameter() throws Exception {
    when(httpServletRequest.getParameter("myKey")).thenReturn("myValue");

    Optional<String> parameter =
        ServletUtils.getRequestParam(httpServletRequest, httpServletResponse, "myKey");
    Assert.assertTrue(parameter.isPresent());
    Assert.assertEquals(parameter.get(), "myValue");
  }

  // Test ServletUtils getRquestParam() function correctly returns empty Optional container when
  // parameter key does not exist.
  @Test
  public void testHTTPRequestGetInvalidParameter() throws Exception {
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    Optional<String> parameter =
        ServletUtils.getRequestParam(httpServletRequest, httpServletResponse, "anInvalidKey");
    Assert.assertTrue(!parameter.isPresent());
    verify(printWriter).println("No anInvalidKey in the query URL.");
  }

  // Test ServletUtils getRquestParam() function correctly returns empty Optional container when
  // provided parameter key is null.
  @Test
  public void testHTTPRequestGetNullParameter() throws Exception {
    when(httpServletResponse.getWriter()).thenReturn(printWriter);

    Optional<String> parameter =
        ServletUtils.getRequestParam(httpServletRequest, httpServletResponse, null);
    Assert.assertTrue(!parameter.isPresent());
    verify(printWriter).println("No null in the query URL.");
  }
}
