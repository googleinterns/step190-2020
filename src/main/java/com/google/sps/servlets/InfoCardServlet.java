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
 * <p>TODO(caseyprice): Write unit tests using Mockito framework
 */
@WebServlet("/info-cards")
public class InfoCardServlet extends HttpServlet {
  private static final String BASE_URL =
      "https://civicinfo.googleapis.com/civicinfo/v2/voterinfo?address=%s&electionId=%s&key=%s";
  private static final String PROJECT_ID = "112408856470";
  private static final String SECRET_MANAGER_ID = "election-api-key";
  private static final String VERSION_ID = "1";

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
    Optional<String> optionalAddress = ServletUtils.getRequestParam(request, response, "address");
    Optional<String> optionalElectionId =
        ServletUtils.getRequestParam(request, response, "electionId");

    if (!optionalAddress.isPresent() || !optionalElectionId.isPresent()) {
      response.setContentType("text/html");
      response
          .getWriter()
          .println("Insufficient parameters to /info-cards. Needs address and electionId.");
      response.setStatus(400);
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Optional<Entity> optionalEntity =
        ServletUtils.findElectionInDatastore(datastore, optionalElectionId.get());

    if (!optionalEntity.isPresent()) {
      response.setContentType("text/html");
      response
          .getWriter()
          .println(
              "Could not find election with ID " + optionalElectionId.get() + " in Datastore.");
      response.setStatus(400);
    }

    Entity electionEntity = optionalEntity.get();
    Election election = Election.fromEntity(electionEntity);
    // Don't need to make the API call if this Election object has already been populated.
    if (election.isPopulatedByVoterInfoQuery()) {
      return;
    }

    String url =
        String.format(
            BASE_URL,
            optionalAddress.get(),
            optionalElectionId.get(),
            ServletUtils.getApiKey(PROJECT_ID, SECRET_MANAGER_ID, VERSION_ID));
    JSONObject voterInfoData = ServletUtils.readFromApiUrl(url);
    election.fromVoterInfoQuery(datastore, voterInfoData).putInDatastore(datastore, electionEntity);
  }
}
