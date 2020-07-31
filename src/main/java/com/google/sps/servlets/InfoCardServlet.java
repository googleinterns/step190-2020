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
import com.google.sps.data.Candidate;
import com.google.sps.data.Contest;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This servlet is used to retrieve the information on the ongoing elections that an eligible voter
 * can participate in on a given day.
 * 
 * TODO(caseyprice): Write unit tests using Mockito framework
 */
@WebServlet("/info-cards")
public class InfoCardServlet extends HttpServlet {

  private static final String BASE_URL = "https://civicinfo.googleapis.com/civicinfo/v2/voterinfo";

  /**
   * Makes an API call to voterInfoQuery in the Google Civic Information API using the user-chosen
   * address and election ID as parameters. Puts Position and Candidate Entities in Datastore from
   * the response. Finds the chosen Election Entity in the Datastore and fills in properties with
   * the corresponding API response data.
   *
   * @param request the HTTP request containing user address and electionId as parameters
   * @param response the HTTP response, contains error message if an error occurs
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
    Entity chosenElection;

    Optional<Entity> foundElection = ServletUtils.findElectionInDatastore(datastore, electionId);
    if (!foundElection.isPresent()) {
      response.setContentType("text/html");
      response
          .getWriter()
          .println(String.format("Could not find election with ID %s in Datastore.", electionId));
      response.setStatus(400);
      return;
    }

    chosenElection = foundElection.get();

    // TODO(anooshree): if this election is already populated, we don't need to make another query
    // TODO(caseyprice): Get Proposition data and add it as a field to the Election Entity.

    URL url =
        new URL(
            String.format(
                "%s?address=%s&electionId=%s&key=%s",
                BASE_URL,
                address,
                electionId,
                ServletUtils.getApiKey("112408856470", "election-api-key", "1")));

    try {
      JSONArray contestListFromApi = ServletUtils.readFromApiUrl(url).getJSONArray("contests");
      populateDatastoreWithContestEntities(datastore, contestListFromApi);
      ArrayList<String> contestKeyList = getEntityKeyNameList(datastore, "Contest");
      chosenElection.setProperty("contests", contestKeyList);
    } catch (JSONException e) {
      chosenElection.setProperty("contests", new ArrayList<String>());
    }
  }

  /**
   * Puts candidate Contest Entities in the Datastore.
   *
   * @param datastore the Datastore containing all election data
   * @param contestData API-returned list of contests on the election ballot
   */
  private void populateDatastoreWithContestEntities(
      DatastoreService datastore, JSONArray contestData) {
    for (Object contestObject : contestData) {
      JSONObject contest = (JSONObject) contestObject;

      populateDatastoreWithCandidateEntities(datastore, contest.getJSONArray("candidates"));

      Entity contestEntity = Contest.fromJSONObject(contest).toEntity();
      contestEntity.setProperty("candidates", getEntityKeyNameList(datastore, "Candidate"));

      datastore.put(contestEntity);
    }
  }

  /**
   * Puts Candidate Entities in the Datastore.
   *
   * @param datastore the Datastore containing all election data
   * @param candidateData API-returned list of candidates for a contest on the election ballot
   * @return list of Key names of all Candidate Entities added
   */
  private void populateDatastoreWithCandidateEntities(
      DatastoreService datastore, JSONArray candidateData) {
    for (Object candidateObject : candidateData) {
      JSONObject candidate = (JSONObject) candidateObject;
      Entity candidateEntity = Candidate.fromJSONObject(candidate).toEntity();
      datastore.put(candidateEntity);
    }
  }

  /**
   * Gets the names of all Keys of a type of Entity in the Datastore.
   *
   * @param datastore the Datastore containing all election data
   * @param entityType the kind of Entity to query
   * @return list of Keys' names of all Contest Entities added
   */
  public ArrayList<String> getEntityKeyNameList(DatastoreService datastore, String entityType) {
    ArrayList<String> keyNameList = new ArrayList<String>();
    Query query = new Query(entityType);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      keyNameList.add(entity.getKey().getName());
    }

    return keyNameList;
  }
}
