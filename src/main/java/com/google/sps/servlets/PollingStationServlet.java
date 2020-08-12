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
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import com.google.sps.data.PollingStation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet is used to retrieve the information on polling stations that an eligible voter can
 * use to vote in a given election.
 */
@WebServlet("/polling-stations")
public class PollingStationServlet extends HttpServlet {

  /**
   * If there is an electionID present in the website URL, this method retrieves the polling
   * stations that can be used to vote in the election represented by that electionID; these polling
   * station objects are instantiated after a call to the InfoCardServlet.
   *
   * @param request the HTTP request containing user address and electionId as parameters
   * @param response the HTTP response, contains error message if an error occurs
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> electionIdOptional =
        ServletUtils.getRequestParam(request, response, "electionId");

    if (!electionIdOptional.isPresent()) {
      return;
    }

    String electionId = electionIdOptional.get();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Optional<Entity> electionEntityOptional =
        ServletUtils.findElectionInDatastore(datastore, electionId);

    if (!electionEntityOptional.isPresent()) {
      response.setContentType("text/html");
      response.getWriter().println("Election with id " + electionId + " was not found.");
      response.setStatus(400);
      return;
    }

    Entity electionEntity = electionEntityOptional.get();

    List<EmbeddedEntity> pollingStationEntities =
        (ArrayList<EmbeddedEntity>) electionEntity.getProperty("pollingStations");
    List<PollingStation> pollingStations = new ArrayList<PollingStation>();

    for (EmbeddedEntity embeddedEntity : pollingStationEntities) {
      Entity entity = new Entity(embeddedEntity.getKey());
      entity.setPropertiesFrom(embeddedEntity);
      pollingStations.add(PollingStation.fromEntity(entity));
    }

    Gson gson = new Gson();
    String json = gson.toJson(pollingStations);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
