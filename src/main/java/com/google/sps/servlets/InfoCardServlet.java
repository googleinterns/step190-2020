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
import com.google.common.collect.ImmutableSet;
import com.google.sps.data.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
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
public final class InfoCardServlet extends HttpServlet {
  private static final String VOTER_QUERY_URL =
      "https://civicinfo.googleapis.com/civicinfo/v2/voterinfo?address=%s&electionId=%s"
          + "&fields=contests(type,office,district,candidates,referendumTitle,referendumSubtitle,referendumUrl,sources)&key=%s";
  private static final String REPRESENTATIVE_QUERY_URL =
      "https://civicinfo.googleapis.com/civicinfo/v2/representatives?address=%s&fields=divisions&key=%s";
  private static final String PROJECT_ID = "112408856470";
  private static final String SECRET_MANAGER_ID = "election-api-key";
  private static final String VERSION_ID = "1";
  private static final String SOURCE_CLASS = InfoCardServlet.class.getName();
  private static final Logger logger = Logger.getLogger(SOURCE_CLASS);
  private static final int SECONDS_PER_HOUR = 3600;

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
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Optional<String> optionalAddress = ServletUtils.getRequestParam(request, response, "address");
    Optional<String> optionalElectionId =
        ServletUtils.getRequestParam(request, response, "electionId");

    if (!optionalAddress.isPresent() || !optionalElectionId.isPresent()) {
      response.setContentType("text/html");
      response
          .getWriter()
          .println("Insufficient parameters to /info-cards. Needs address and electionId.");
      response.setStatus(400);
      return;
    }

    String address = optionalAddress.get();
    String electionId = optionalElectionId.get();

    String divisionsCallUrl =
        String.format(
                REPRESENTATIVE_QUERY_URL,
                address,
                ServletUtils.getApiKey(PROJECT_ID, SECRET_MANAGER_ID, VERSION_ID))
            .replaceAll(" ", "%20");
    Optional<JSONObject> divisionsInfoData =
        ServletUtils.readFromApiUrl(divisionsCallUrl, /* isXml= */ false);

    if (!divisionsInfoData.isPresent()) {
      response.setContentType("text/html");
      response
          .getWriter()
          .println("Could not get division information for address " + address + ".");
      response.setStatus(400);
      return;
    }

    JSONObject addressDivisions = divisionsInfoData.get().getJSONObject("divisions");

    Optional<Entity> optionalEntity = ServletUtils.findElectionInDatastore(datastore, electionId);

    if (!optionalEntity.isPresent()) {
      response.setContentType("text/html");
      response
          .getWriter()
          .println("Could not find election with ID " + electionId + " in Datastore.");
      response.setStatus(400);
      return;
    }

    logger.logp(Level.INFO, SOURCE_CLASS, "doPut", "Got election ID and user address.");

    Entity electionEntity = optionalEntity.get();
    Election election = Election.fromEntity(electionEntity);

    ImmutableSet<String> divisionsQueried = ImmutableSet.copyOf(election.getDivisions());
    ImmutableSet<String> addressDivisionsSet = ImmutableSet.copyOf(addressDivisions.keys());

    response.addCookie(generateDivisionsCookie(addressDivisionsSet));

    if (divisionsQueried.containsAll(addressDivisionsSet)) {
      logger.logp(
          Level.INFO,
          SOURCE_CLASS,
          "doPut",
          "Address ballot info already in datastore. Exiting doPut.");
      return;
    }

    HashSet<String> updatedDivisions = new HashSet<>(divisionsQueried);
    ImmutableSet<String> newDivisions =
        getRelativeComplementSet(addressDivisionsSet, divisionsQueried);
    updatedDivisions.addAll(newDivisions);
    electionEntity.setProperty("divisions", updatedDivisions);
    electionEntity.setProperty("contests", new HashSet<Long>());
    electionEntity.setProperty("referendums", new HashSet<Long>());

    String url =
        String.format(
                VOTER_QUERY_URL,
                address,
                electionId,
                ServletUtils.getApiKey(PROJECT_ID, SECRET_MANAGER_ID, VERSION_ID))
            .replaceAll(" ", "%20");

    Optional<JSONObject> voterInfoData = ServletUtils.readFromApiUrl(url, /* isXml= */ false);
    if (!voterInfoData.isPresent()) {
      response.setContentType("text/html");
      response
          .getWriter()
          .println(
              "Could not query with electionId " + electionId + " and address " + address + ".");
      response.setStatus(400);
      return;
    }

    logger.logp(Level.INFO, SOURCE_CLASS, "doPut", "Performing PUT on election from API response.");
    election
        .fromVoterInfoQuery(datastore, voterInfoData.get(), newDivisions)
        .putInDatastore(datastore, electionEntity);
    logger.logp(Level.INFO, SOURCE_CLASS, "doPut", "PUT /info-cards is complete.");
  }

  /** Helper function that saves divisions returned by representative query in a cookie. */
  private static Cookie generateDivisionsCookie(Set<String> divisions) {
    String divisionsString =
        StreamSupport.stream(divisions.spliterator(), false).collect(Collectors.joining("|"));
    Cookie divisionsCookie = new Cookie("addressDivisions", divisionsString);
    divisionsCookie.setPath("/contests");
    divisionsCookie.setVersion(1);
    divisionsCookie.setMaxAge(SECONDS_PER_HOUR);
    divisionsCookie.setComment("Electoral divisions the queried address belongs to");
    return divisionsCookie;
  }

  /** Returns the relative complement of secondSet with respect to firstSet. */
  private static ImmutableSet<String> getRelativeComplementSet(
      Set<String> firstSet, Set<String> secondSet) {
    HashSet<String> copyOfFirstSet = new HashSet<>(firstSet);
    copyOfFirstSet.removeAll(secondSet);
    return ImmutableSet.copyOf(copyOfFirstSet);
  }
}
