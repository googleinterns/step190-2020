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

  /**
   * Makes an API call to voterInfoQuery in the Google Civic Information API and puts Position and
   * Candidate Entities in Datastore from the response. Finds the chosen Election Entity in the
   * Datastore and fills is properties with the corresponding API response data.
   *
   * <p>TODO: Get Proposition data and add it as a field to the Election Entity.
   *
   * @param request the HTTP request containing user address and electionId as parameters
   * @param response the HTTP response, contains error message if an error occurs
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Read in the user-chosen address and election ID, to be used as parameters to the
    // voterInfoQuery. If these parameters aren't found, API call can't be performed so return early
    // with error
    // message.
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

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity chosenElection = ServletHelper.findElectionInDatastore(datastore, electionId);

    if (chosenElection == null) {
      response.setContentType("text/html");
      response
          .getWriter()
          .println(String.format("Could not find election with ID %s in Datastore.", electionId));
      response.setStatus(400);
      return;
    }

    // TODO: if this election is already populated, we don't need to make another query

    URL url =
        new URL(
            String.format(
                "%s?address=%s&electionId=%s&key=%s",
                BASE_URL,
                address,
                electionId,
                ServletHelper.getApiKey("112408856470", "election-api-key", "1")));
    JSONObject electionListFromApi = ServletHelper.readFromApiUrl(url);
    JSONArray positionListFromApi = electionListFromApi.getJSONArray("contests");
    chosenElection.setProperty("positions", fillPositions(datastore, positionListFromApi));
  }

  /**
   * Puts candidate Position Entities in the Datastore.
   *
   * @param datastore the Datastore containing all election data
   * @param positionData API-returned list of positions on the election ballot
   * @return list of Key names of all Position Entities added
   */
  private List<String> fillPositions(DatastoreService datastore, JSONArray positionData) {
    List<String> positionKeyList = new ArrayList<String>();
    for (Object positionObject : positionData) {
      JSONObject position = (JSONObject) positionObject;

      Entity positionEntity = new Entity("Position");
      positionEntity.setProperty("name", position.getString("office"));
      positionEntity.setProperty(
          "candidates", fillCandidates(datastore, position.getJSONArray("candidates")));
      datastore.put(positionEntity);

      positionKeyList.add(positionEntity.getKey().getName());
    }

    return positionKeyList;
  }

  /**
   * Puts Candidate Entities in the Datastore.
   *
   * @param datastore the Datastore containing all election data
   * @param candidateData API-returned list of candidates for a position on the election ballot
   * @return list of Key names of all Candidate Entities added
   */
  private List<String> fillCandidates(DatastoreService datastore, JSONArray candidateData) {
    List<String> candidateKeyList = new ArrayList<String>();
    for (Object candidateObject : candidateData) {
      JSONObject candidate = (JSONObject) candidateObject;

      Entity candidateEntity = new Entity("Candidate");
      candidateEntity.setProperty("name", candidate.getString("name"));
      candidateEntity.setProperty("partyAffiliation", candidate.getString("party"));
      datastore.put(candidateEntity);

      candidateKeyList.add(candidateEntity.getKey().getName());
    }

    return candidateKeyList;
  }
}
