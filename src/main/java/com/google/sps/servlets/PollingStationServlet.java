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
import com.google.sps.data.Election;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * This servlet is used to retrieve the information on the ongoing elections that an eligible voter
 * can participate in on a given day.
 *
 * TODO(anooshree): Write unit tests using Mockito framework
 */
@WebServlet("/polling-stations")
public class PollingStationsServlet extends HttpServlet {

  /**
   * Retrieves all PollingStation entities currently stored in Datastore following processing
   * by InfoCardServlets. If there is an electionID present in the website URL, this method
   * retrieves only the polling stations that can be used to vote in the election represented
   * by that electionID.
   *
   * @param request the HTTP request containing user address and electionId as parameters
   * @param response the HTTP response, contains error message if an error occurs
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> optionalId = ServletUtils.getRequestParam(request, response, "electionID");

    Query query = new Query("PollingStation");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<PollingStation> pollingStations = new ArrayList<PollingStation>();

    for (Entity entity : results.asIterable()) {
      pollingStations.add(PollingStation.fromEntity(entity));
    }

    Gson gson = new Gson();
    String json = gson.toJson(pollingStations);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
