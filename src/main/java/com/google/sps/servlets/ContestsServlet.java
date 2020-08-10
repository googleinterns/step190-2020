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
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.sps.data.Contest;
import com.google.sps.data.Election;
import com.google.sps.data.Referendum;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    Set<Long> electionContestsIds = ImmutableSet.copyOf(election.getContests());
    Set<Long> electionReferendumIds = ImmutableSet.copyOf(election.getReferendums());
    List<Contest> contestList = new ArrayList<>();
    List<Referendum> referendumList = new ArrayList<>();

    for (Long contestId : electionContestsIds) {
      Key currentKey = KeyFactory.createKey(Contest.ENTITY_KIND, contestId.longValue());
      try {
        Entity currentContestEntity = datastore.get(currentKey);
        contestList.add(Contest.fromEntity(currentContestEntity));
      } catch (EntityNotFoundException e) {
        response.setContentType("text/html");
        response.getWriter().println("Contest with Id " + contestId.toString() + " was not found.");
        response.setStatus(400);
        return;
      }
    }

    for (Long referendumId : electionReferendumIds) {
      Key currentKey = KeyFactory.createKey(Referendum.ENTITY_KIND, referendumId.longValue());
      try {
        Entity currentReferendumEntity = datastore.get(currentKey);
        referendumList.add(Referendum.fromEntity(currentReferendumEntity));
      } catch (EntityNotFoundException e) {
        response.setContentType("text/html");
        response
            .getWriter()
            .println("Referendum with Id " + referendumId.toString() + " was not found.");
        response.setStatus(400);
        return;
      }
    }

    Gson gson = new Gson();
    String contestJson = gson.toJson(contestList);
    String referendumJson = gson.toJson(referendumList);

    response.setContentType("application/json;");
    response
        .getWriter()
        .println(
            "{\""
                + Election.CONTESTS_ENTITY_KEYWORD
                + "\": "
                + contestJson
                + ",\""
                + Election.REFERENDUMS_ENTITY_KEYWORD
                + "\": "
                + referendumJson
                + "}");
  }
}
