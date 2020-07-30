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
import java.io.IOException;
import java.net.URL;
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
 */
@WebServlet("/info-cards")
public class InfoCardServlet extends HttpServlet {

  private static final String BASE_URL = "https://civicinfo.googleapis.com/civicinfo/v2/voterinfo";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // find the corresponding election and check fields
    // TODO (anooshree): iterate through Datastore and find the election
    //                   with the matching electionId
    // if this election is already populated, we don't need to make another query

    String address = request.getParameter("address");
    if (address == null) {
      response.setContentType("text/html");
      response
          .getWriter()
          .println("No address in the query URL, please check why this is the case.");
      response.setStatus(400);
      return;
    }

    String electionId = request.getParameter("electionId");
    if (electionId == null) {
      response.setContentType("text/html");
      response
          .getWriter()
          .println("No election ID in the query URL, please check why this is the case.");
      response.setStatus(400);
      return;
    }

    URL url =
        new URL(
            String.format(
                "%s?address=%s&electionId=%s&key=%s",
                BASE_URL,
                address,
                electionId,
                ServletHelper.getApiKey("112408856470", "election-api-key", "1")));

    // TODO(anooshree, caseyprice): process JSON objects from API to store as
    //                              Datastore entities using decomposed functions
    // TODO(caseyprice): populate the election entity by mapping to the
    //                   candidates and propositions on the ballot
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Election");
    PreparedQuery results = datastore.prepare(query);

    JSONObject obj = ServletHelper.readFromApiUrl(url, response);

    for (Entity entity : results.asIterable()) {
      if (entity.getProperty("id") == electionId) {
        entity.setProperty("positions", fillPositions(datastore, obj.getJSONArray("contests")));
        break;
      }
    }
  }

  /** */
  private List<String> fillPositions(DatastoreService datastore, JSONArray positionData) {
    List<String> positionIdList = new ArrayList<String>();
    for (Object positionObject : positionData) {
      JSONObject position = (JSONObject) positionObject;

      Entity positionEntity = new Entity("Position");
      positionEntity.setProperty("name", position.getString("office"));
      positionEntity.setProperty(
          "candidates", fillCandidates(datastore, position.getJSONArray("candidates")));
      datastore.put(positionEntity);

      positionIdList.add(positionEntity.getKey().getName());
    }

    return positionIdList;
  }

  private List<String> fillCandidates(DatastoreService datastore, JSONArray candidateData) {
    // Candidates for this Position are also Entities in Datastore; save their
    // ID's in a list for this Position to reference.
    List<String> candidateIdList = new ArrayList<String>();
    for (Object candidateObject : candidateData) {
      JSONObject candidate = (JSONObject) candidateObject;

      Entity candidateEntity = new Entity("Candidate");
      candidateEntity.setProperty("name", candidate.getString("name"));
      candidateEntity.setProperty("partyAffiliation", candidate.getString("party"));
      datastore.put(candidateEntity);

      candidateIdList.add(candidateEntity.getKey().getName());
    }

    return candidateIdList;
  }
}
