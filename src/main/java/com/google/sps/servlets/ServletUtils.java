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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

public class ServletUtils {

  private static final Logger logger = Logger.getLogger(ServletUtils.class.getName());

  /** Access the api key stored in gcloud secret manager. */
  public static String getApiKey(String projectId, String secretId, String versionId)
      throws IOException {
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

      return response.getPayload().getData().toStringUtf8();
    }
  }

  public static JSONObject readFromApiUrl(URL url) throws IOException {
    StringBuilder strBuf = new StringBuilder();
    HttpURLConnection conn = null;
    BufferedReader reader = null;

    try {
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
      logger.log(Level.SEVERE, "URL is incorrectly formatted");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Cannot retrieve information from provided URL");
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

    String results = strBuf.toString();
    JSONObject obj = new JSONObject(results);

    return obj;
  }

  /**
   * Find an Election Entity in the Datastore based off its electionId property.
   *
   * @param datastore the Datastore containing all election data
   * @param electionId the ID of the election being queried
   * @return the Election Entity if found, null otherwise
   */
  public static Optional<Entity> findElectionInDatastore(
      DatastoreService datastore, String electionId) {
    Query query = new Query("Election");
    PreparedQuery results = datastore.prepare(query);
    Entity targetElection = null;

    for (Entity entity : results.asIterable()) {
      if (entity.getProperty("id") == electionId) {
        targetElection = entity;
      }
    }

    return Optional.ofNullable(targetElection);
  }
}
