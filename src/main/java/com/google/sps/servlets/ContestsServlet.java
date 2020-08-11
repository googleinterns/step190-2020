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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.sps.data.Contest;
import com.google.sps.data.Election;
import com.google.sps.data.Referendum;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet is used to retrieve the information of all the contests present on the election
 * specified by query parameter.
 */
@WebServlet("/contests")
public class ContestsServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> electionIdOptional =
        ServletUtils.getRequestParam(request, response, "electionId");

    if (!electionIdOptional.isPresent()) {
      return;
    }

    String electionId = electionIdOptional.get();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Optional<Entity> electionEntityOptional =
        ServletUtils.findElectionInDatastore(datastore, electionId);

    if (!electionEntityOptional.isPresent()) {
      response.setContentType("text/html");
      response.getWriter().println("Election with id " + electionId + " was not found.");
      response.setStatus(400);
      return;
    }

    Election election = Election.fromEntity(electionEntityOptional.get());

    List<JsonElement> contestJsonList =
        ImmutableSet.copyOf(election.getContests())
            .stream()
            .map(id -> KeyFactory.createKey(Contest.ENTITY_KIND, id.longValue()))
            .map(key -> ServletUtils.getFromDatastore(datastore, key))
            .map(entity -> Contest.fromEntity(entity).toJsonString(datastore))
            .map(jsonString -> JsonParser.parseString(jsonString))
            .collect(ImmutableList.toImmutableList());


    List<JsonElement> referendumJsonList =
        ImmutableSet.copyOf(election.getReferendums())
            .stream()
            .map(id -> KeyFactory.createKey(Referendum.ENTITY_KIND, id.longValue()))
            .map(key -> ServletUtils.getFromDatastore(datastore, key))
            .map(entity -> Referendum.fromEntity(entity).toJsonString())
            .map(jsonString -> JsonParser.parseString(jsonString))
            .collect(ImmutableList.toImmutableList());

    Gson gson = new Gson();
    String contestJson =
        gson.toJson(contestJsonList).replace("\\", "").replace("\"{", "{").replace("}\"", "}");
    String referendumJson =
        gson.toJson(referendumJsonList).replace("\\", "").replace("\"{", "{").replace("}\"", "}");

    response.setContentType("application/json;");
    response
        .getWriter()
        .println(
            "{\""
                + Election.CONTESTS_ENTITY_KEYWORD
                + "\":"
                + contestJson
                + ",\""
                + Election.REFERENDUMS_ENTITY_KEYWORD
                + "\":"
                + referendumJson
                + "}");
  }
}
