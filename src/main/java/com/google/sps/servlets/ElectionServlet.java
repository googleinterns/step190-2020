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
import com.google.gson.Gson;
import com.google.sps.data.Election;
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
@WebServlet("/election")
public class ElectionServlet extends HttpServlet {

  private static final String BASE_URL = "https://www.googleapis.com/civicinfo/v2/elections?key=";

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Deleting the queries from yesterday in the case that they are outdated
    Query query = new Query("Election");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      Key electionEntityKey = KeyFactory.createKey("Election", entity.getKey().getId());
      datastore.delete(electionEntityKey);
    }

    String electionApiKey = ServletHelper.getApiKey("112408856470", "election-api-key", "1");
    URL url = new URL(BASE_URL + electionApiKey);

    JSONObject obj = ServletHelper.readFromApiUrl(url, response);
    JSONArray electionData = obj.getJSONArray("elections");

    for (Object o : electionData) {
      JSONObject election = (JSONObject) o;

      /* The "id" of an Election Entity is stored as a property instead of
       * replacing the Datastore-generated ID because  Datastore may
       * accidentally reassign IDs to other entities. To avoid this problem, I would have
       * to obtain a block of IDs with allocateIds(), but this is also difficult because
       * election IDs are not always consecutive numbers and other entities we plan to store
       * in Datastore will not have IDs from the Civic Information API (ex. policies) */
      Entity electionEntity = new Entity("Election");
      electionEntity.setProperty("electionId", election.getLong("id"));
      electionEntity.setProperty("name", election.getString("name"));
      electionEntity.setProperty("scope", election.getString("ocdDivisionId"));
      electionEntity.setProperty("date", election.getString("electionDay"));

      datastore.put(electionEntity);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Election");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Election> elections = new ArrayList<Election>();

    for (Entity entity : results.asIterable()) {
      long id = (long) entity.getProperty("id");
      String name = (String) entity.getProperty("name");
      String scope = (String) entity.getProperty("scope");
      String date = (String) entity.getProperty("date");

      Election newElection =
          Election.builder().setId(id).setName(name).setScope(scope).setDate(date).build();
      elections.add(newElection);
    }

    Gson gson = new Gson();
    String json = gson.toJson(elections);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
