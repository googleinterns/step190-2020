// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.sps.data.Election;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This servlet is used to retrieve the information on the ongoing elections that an eligible voter
 * can participate in on a given day.
 *
 * <p>TODO(anooshree): Write unit tests using the Power Mockito framework
 */
@WebServlet("/election")
public class ElectionServlet extends HttpServlet {

  private static final String BASE_URL = "https://www.googleapis.com/civicinfo/v2/elections?key=%s";

  /**
   * Makes an API call to electionQuery in the Google Civic Information API. Puts Election Entities
   * in Datastore from the response.
   *
   * @param request the HTTP request containing user address and electionId as parameters
   * @param response the HTTP response, contains error message if an error occurs
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    ServletUtils.deleteAllEntitiesOfKind(datastore, Election.ENTITY_KIND);

    String electionApiKey = ServletUtils.getApiKey("112408856470", "election-api-key", "1");

    JSONObject obj = ServletUtils.readFromApiUrl(String.format(BASE_URL, electionApiKey));
    JSONArray electionQueryArray = obj.getJSONArray(Election.ELECTIONS_JSON_KEYWORD);

    for (Object o : electionQueryArray) {
      JSONObject election = (JSONObject) o;
      // TODO(anooshree): store Key name returned by addToDatastore(), to be used in PollingStation.
      Election.fromElectionQuery(election).addToDatastore(datastore);
    }
  }

  /**
   * Retrieves the list of elections still open for voting on a given day in the form of a JSON
   * object
   *
   * @param request the HTTP request containing user address and electionId as parameters
   * @param response the HTTP response, contains error message if an error occurs
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(Election.ENTITY_KIND);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Election> elections = new ArrayList<Election>();

    for (Entity entity : results.asIterable()) {
      elections.add(Election.fromEntity(entity));
    }

    Gson gson = new Gson();
    String json = gson.toJson(elections);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
