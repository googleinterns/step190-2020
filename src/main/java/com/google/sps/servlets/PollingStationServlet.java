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
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This servlet is used to retrieve the information on polling stations that an eligible voter can
 * use to vote in a given election.
 */
@WebServlet("/polling-stations")
public final class PollingStationServlet extends HttpServlet {

  private static final String POLLING_STATION_QUERY_URL =
      "https://civicinfo.googleapis.com/civicinfo/v2/voterinfo?address=%s&electionId=%s"
          + "&fields=pollingLocations,earlyVoteSites,dropOffLocations&key=%s";
  private static final String PROJECT_ID = "112408856470";
  private static final String SECRET_MANAGER_ID = "election-api-key";
  private static final String VERSION_ID = "1";

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

    Optional<String> addressOptional = ServletUtils.getRequestParam(request, response, "address");

    if (!electionIdOptional.isPresent() || !addressOptional.isPresent()) {
      return;
    }

    String electionId = electionIdOptional.get();
    String address = addressOptional.get();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    String url =
        String.format(
                POLLING_STATION_QUERY_URL,
                address,
                electionId,
                ServletUtils.getApiKey(PROJECT_ID, SECRET_MANAGER_ID, VERSION_ID))
            .replaceAll(" ", "%20");

    Optional<JSONObject> pollingInfoData = ServletUtils.readFromApiUrl(url, /* isXml= */ false);
    List<PollingStation> pollingStations = new ArrayList<PollingStation>();

    if (!pollingInfoData.isPresent()) {
      response.setContentType("text/html");
      response.getWriter().println("Polling locations for " + address + " were not found.");
      response.setStatus(400);
      return;
    }

    JSONObject pollingInfo = pollingInfoData.get();

    addToPollingStationList(pollingStations, "earlyVoteSites", pollingInfo);
    addToPollingStationList(pollingStations, "dropOffLocation", pollingInfo);
    addToPollingStationList(pollingStations, "pollingLocations", pollingInfo);

    Gson gson = new Gson();
    String json = gson.toJson(pollingStations);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  private static void addToPollingStationList(
      List<PollingStation> pollingStations, String locationType, JSONObject pollingInfo) {
    if (pollingInfo.has(locationType)) {
      JSONArray pollingData = pollingInfo.getJSONArray(locationType);
      for (Object pollingLocation : pollingData) {
        JSONObject pollingLocationJSON = (JSONObject) pollingLocation;
        pollingStations.add(PollingStation.fromJSONObject(pollingLocationJSON, locationType));
      }
    }
  }
}
