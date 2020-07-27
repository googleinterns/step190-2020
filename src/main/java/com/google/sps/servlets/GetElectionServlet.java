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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/getElection")

/**
 * This servlet is used to update the gVote database with information on the ongoing elections that
 * an eligible voter can participate in on a given day.
 */
public class GetElectionServlet extends HttpServlet {

  private static final String baseURL = "https://www.googleapis.com/civicinfo/v2/elections?key=";
  private static final Logger logger = Logger.getLogger(ElectionServlet.class.getName());

  // This method is used to access the api key stored in gcloud secret manager.
  public String getApiKey(String projectId, String secretId, String versionId) throws IOException {
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

      return response.getPayload().getData().toStringUtf8();
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Deleting the queries from yesterday in the case that they are outdated
    Query query = new Query("Election");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      Key electionEntityKey = KeyFactory.createKey("Election", entity.getKey().getId());
      datastore.delete(electionEntityKey);
    }

    StringBuilder strBuf = new StringBuilder();
    HttpURLConnection conn = null;
    BufferedReader reader = null;

    try {
      // TODO(anooshree): Figure out how to control this difference via flag
      // Running on a local server requires storing API key in environment variable.
      // In this case, uncomment the following line, and comment out the call to
      // accessSecretVersion. Do the reverse when deploying.
      // String electionApiKey = System.getenv("GOOGLE_API_KEY");

      String electionApiKey = getApiKey("112408856470", "election-api-key", "1");
      URL url = new URL(baseURL + electionApiKey);
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

    String elections = strBuf.toString();

    JSONObject obj = new JSONObject(elections);
    JSONArray electionData = obj.getJSONArray("elections");
    int numElections = electionData.length();

    for (int i = 0; i < numElections; ++i) {
      JSONObject currentElection = electionData.getJSONObject(i);
      Entity electionEntity = new Entity("Election");

      /* The "id" of an Election Entity is stored as a property instead of
       * replacing the Datastore-generated ID because  Datastore may
       * accidentally reassign IDs to other entities. To avoid this problem, I would have
       * to obtain a block of IDs with allocateIds(), but this is also difficult because
       * election IDs are not always consecutive numbers and other entities we plan to store
       * in Datastore will not have IDs from the Civic Information API (ex. policies) */
      electionEntity.setProperty("id", currentElection.getLong("id"));
      electionEntity.setProperty("name", currentElection.getString("name"));
      electionEntity.setProperty("scope", currentElection.getString("ocdDivisionId"));
      electionEntity.setProperty("date", currentElection.getString("electionDay"));

      datastore.put(electionEntity);
    }

    Gson gson = new Gson();
    String json = gson.toJson(elections);

    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  // TODO(anooshree): Figure out if doGet should remain nested within doPut or
  // a) if the doGet method could have a return type that could be passed to the doPut and
  // b) if the cron job should be calling a doGet when putting something in the Datastore.
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    doGet(request, response);
  }
}
