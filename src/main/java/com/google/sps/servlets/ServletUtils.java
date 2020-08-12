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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.sps.data.Election;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

public class ServletUtils {

  private static final Logger logger = Logger.getLogger(ServletUtils.class.getName());

  // Private constructor to prevent instantiation.
  private ServletUtils() {
    throw new AssertionError();
  }

  /**
   * Get the value of a query parameter to an HTTP request.
   *
   * @param request the HTTP request to be serialized
   * @param response the HTTP response to publish any error messages
   * @param inputName the query parameter key
   * @return Optional container that contains either the parameter or null
   */
  public static Optional<String> getRequestParam(
      HttpServletRequest request, HttpServletResponse response, String inputName)
      throws IOException {

    String input = request.getParameter(inputName);

    if (input == null) {
      response.setContentType("text/html");
      response.getWriter().println(String.format("No %s in the query URL.", inputName));
      response.setStatus(400);
      return Optional.empty();
    }

    return Optional.of(input);
  }

  /**
   * Access the api key stored in gcloud secret manager.
   *
   * @param projectId the GCP project ID, available through the Cloud Dashboard
   * @param secretId the ID of the "secret" containing the API key in GCP Secret Manager
   * @param versionId the version of the secret we want to access
   */
  public static String getApiKey(String projectId, String secretId, String versionId)
      throws IOException {
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

      return response.getPayload().getData().toStringUtf8();
    }
  }

  /**
   * Reads the information avaiable at the provided API URL into a JSON object
   *
   * @param urlString a valid API URL, accessible with the project's API keys
   */
  public static JSONObject readFromApiUrl(String urlString) throws IOException {
    StringBuilder strBuf = new StringBuilder();
    HttpURLConnection conn = null;
    BufferedReader reader = null;
    URL url = new URL(urlString);

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

  public static void deleteAllEntitiesOfKind(DatastoreService datastore, String entityKind) {
    // Deleting the queries from yesterday in the case that they are outdated
    PreparedQuery results = datastore.prepare(new Query(entityKind));

    for (Entity entity : results.asIterable()) {
      Key entityKey = KeyFactory.createKey(entityKind, entity.getKey().getId());
      datastore.delete(entityKey);
    }
  }

  /**
   * Find an Election Entity in the Datastore based off its electionId property.
   *
   * @param datastore the Datastore containing all election data
   * @param electionId the ID of the election being queried
   * @return Optional container that contains either the Election entity's Key or null
   */
  public static Optional<Entity> findElectionInDatastore(
      DatastoreService datastore, String electionId) {
    Query query = new Query(Election.ENTITY_KIND);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      if (entity.getProperty(Election.ID_ENTITY_KEYWORD).equals(electionId)) {
        return Optional.of(entity);
      }
    }

    return Optional.empty();
  }

  /**
   * Queries Datastore for a given Entity key and instantiates an Optional container if it's present
   * in Datastore
   *
   * @param datastore the Datastore containing all election data
   * @param key the key corresponding to the Entity being queried
   * @return the Optional<Entity>
   */
  public static Optional<Entity> getFromDatastore(DatastoreService datastore, Key key) {
    try {
      return Optional.of(datastore.get(key));
    } catch (EntityNotFoundException e) {
      return Optional.empty();
    }
  }
}
