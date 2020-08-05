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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.sps.data.Contest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet is used to retrieve the information on the ongoing elections that an eligible voter
 * can participate in on a given day.
 */
@WebServlet("/contests")
public class ContestsServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int electionId = Integer.parseInt(request.getParameter("electionId"));
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity electionEntity;

    try {
      electionEntity = getElectionEntityFromId(datastore, electionId);
    } catch (Exception e) {
      response.getWriter().println(e.getMessage());
      return;
    }

    Collection<Long> electionContestsIds =
        (Collection<Long>) electionEntity.getProperty("contests");
    List<Contest> contestList = new ArrayList<>();

    for (Long contestId : electionContestsIds) {
      Key currentKey = KeyFactory.createKey("Contest", contestId.longValue());
      try {
        Entity currentContestEntity = datastore.get(currentKey);
        contestList.add(Contest.fromEntity(currentContestEntity));
      } catch (EntityNotFoundException e) {
        response.getWriter().println("Contest was not found.");
        return;
      }
    }

    Gson gson = new Gson();
    String json = gson.toJson(contestList);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  // Given an election ID return the corresponding election from datastore
  private static Entity getElectionEntityFromId(DatastoreService datastore, int electionId)
      throws Exception {
    Query electionQuery = new Query("Election");
    PreparedQuery results = datastore.prepare(electionQuery);
    Entity currentElection = null;
    for (Entity electionEntity : results.asIterable()) {
      int currentElectionId = Integer.parseInt((String) electionEntity.getProperty("id"));
      if (electionId == currentElectionId) {
        currentElection = electionEntity;
        break;
      }
    }

    if (currentElection == null) {
      throw new Exception("No election with the provided election ID found.");
    }

    return currentElection;
  }
}
