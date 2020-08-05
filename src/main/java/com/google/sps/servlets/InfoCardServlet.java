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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.gson.Gson;
import com.google.sps.data.Election;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/info-cards")

/**
 * This servlet is used to retrieve the information on the ongoing elections that an eligible voter
 * can participate in on a given day.
 */
public class InfoCardServlet extends HttpServlet {

  private static final String BASE_URL = "https://civicinfo.googleapis.com/civicinfo/v2/voterinfo?";
  private static final Logger logger = Logger.getLogger(ElectionServlet.class.getName());

  // This method is used to access the api key stored in gcloud secret manager.
  public String getApiKey(String projectId, String secretId, String versionId) throws IOException {
    // TODO(anooshree): Figure out how to control this difference via flag

    // Running on a local server requires storing API key in environment variable.
    // In this case, uncomment the following line, and comment out the remaining lines
    // in this function. Do the reverse when deploying.
    // return System.getenv("GOOGLE_API_KEY");

    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

      return response.getPayload().getData().toStringUtf8();
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// find the corresponding election and check fields
		// TODO (anooshree): iterate through Datastore and find the election
		//									 with the matching electionId
		// if this election is already populated, we don't need to make another
		// query

		// fetchElection(electionId)

    StringBuilder strBuf = new StringBuilder();
    HttpURLConnection conn = null;
    BufferedReader reader = null;

    try {
      String address = request.getParameter("address");
			if (address == null) { 
				response.setContentType("text/html");
      	response.getWriter().println("No address in the query URL, please check why this is the case.");
      	return;
			}

			String electionId = request.getParameter("electionId");
			if (electionId == null) { 
				response.setContentType("text/html");
      	response.getWriter().println("No election ID in the query URL, please check why this is the case.");
      	return;
			}

      URL url = new URL(BASE_URL + "address=" + address + "&electionId=" + electionId + "&key=" + 
												getApiKey("112408856470", "election-api-key", "1"));

      conn = (HttpURLConnection) url.openConnection();

      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "application/json");

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new RuntimeException(
            "HTTP GET Request Failed with Error code : " + conn.getResponseCode());
      }

      // Using IO Stream with Buffer for increased efficiency
      reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
      String output = null;

      while ((output = reader.readLine()) != null) {
        strBuf.append(output);
      }
    } catch (MalformedURLException e) {
      response.setContentType("text/html");
      response.getWriter().println("URL is incorrectly formatted");
      return;
    } catch (IOException e) {
      response.setContentType("text/html");
      response.getWriter().println("Cannot retrieve information from provided URL");
      return;
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, e.getMessage());
        }
      }
      if (conn != null) {
        conn.disconnect();
      }
    }

<<<<<<< Updated upstream
    String results = strBuf.toString();
    JSONObject obj = new JSONObject(results);

    /*JSONArray pollingLocationData = obj.getJSONArray("pollingLocations");
		processLocations(pollingLocationData, "polling");
		
		JSONArray dropOffLocationData = obj.getJSONArray("dropOffLocations");
		processLocations(dropOffLocationData, "dropOff");

		JSONArray earlyVoteSiteData = obj.getJSONArray("earlyVoteSites");
		processLocations(earlyVoteSiteData, "earlyVote");

		JSONArray contestData = obj.getJSONArray("contests");
		processCandidatesAndPropositions(contestData, electionId);*/

		// TODO(caseyprice): populate the election entity by mapping to the 
		// 									 candidates and propositions on the ballot

=======
    String url =
        String.format(
            BASE_URL,
            optionalAddress.get(),
            optionalElectionId.get(),
            ServletUtils.getApiKey(PROJECT_ID, SECRET_MANAGER_ID, VERSION_ID));
    JSONObject voterInfoData = ServletUtils.readFromApiUrl(url);

    election.fromVoterInfoQuery(datastore, voterInfoData).putInDatastore(datastore, electionEntity);
    // TODO(anooshree): write similar method for PollingStations object that reads in polling information
    //                  and writes entities to Datastore
    
>>>>>>> Stashed changes
  }
}
