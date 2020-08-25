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

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/** This servlet is used to retrieve the information on the voting deadlines for a user's state. */
@WebServlet("/deadlines")
public final class DeadlinesServlet extends HttpServlet {

  private static final ImmutableMap<String, String> STATE_MAP =
      ImmutableMap.<String, String>builder()
          .put("al", "Alabama")
          .put("ak", "Alaska")
          .put("ab", "Alberta")
          .put("az", "Arizona")
          .put("ar", "Arkansas")
          .put("ca", "California")
          .put("co", "Colorado")
          .put("ct", "Connecticut")
          .put("de", "Delaware")
          .put("dc", "District Of Columbia")
          .put("fl", "Florida")
          .put("ga", "Georgia")
          .put("gu", "Guam")
          .put("hi", "Hawaii")
          .put("id", "Idaho")
          .put("il", "Illinois")
          .put("in", "Indiana")
          .put("ia", "Iowa")
          .put("ks", "Kansas")
          .put("ky", "Kentucky")
          .put("la", "Louisiana")
          .put("me", "Maine")
          .put("md", "Maryland")
          .put("ma", "Massachusetts")
          .put("mi", "Michigan")
          .put("mn", "Minnesota")
          .put("ms", "Mississippi")
          .put("mo", "Missouri")
          .put("mt", "Montana")
          .put("ne", "Nebraska")
          .put("nv", "Nevada")
          .put("nh", "New Hampshire")
          .put("nj", "New Jersey")
          .put("nm", "New Mexico")
          .put("ny", "New York")
          .put("nc", "North Carolina")
          .put("nd", "North Dakota")
          .put("oh", "Ohio")
          .put("ok", "Oklahoma")
          .put("or", "Oregon")
          .put("pa", "Pennsylvania")
          .put("pr", "Puerto Rico")
          .put("ri", "Rhode Island")
          .put("sc", "South Carolina")
          .put("sd", "South Dakota")
          .put("tn", "Tennessee")
          .put("tx", "Texas")
          .put("ut", "Utah")
          .put("vt", "Vermont")
          .put("vi", "Virgin Islands")
          .put("va", "Virginia")
          .put("wa", "Washington")
          .put("wv", "West Virginia")
          .put("wi", "Wisconsin")
          .put("wy", "Wyoming")
          .build();

  private static final String BASE_URL = "https://fvap.gov/xml-api";
  private static final String DEADLINES_PARAM = "deadline-dates.xml";

  /**
   * Retrieves the deadllines for mail in voting and registration for a given state using the FVAP
   * API
   *
   * @param request the HTTP request containing the user's state
   * @param response the HTTP response, contains error message if an error occurs
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> stateOptional = ServletUtils.getRequestParam(request, response, "state");

    if (!stateOptional.isPresent()) {
      return;
    }

    if (!STATE_MAP.containsKey(stateOptional.get())) {
      response.setContentType("text/html");
      response
          .getWriter()
          .println(String.format("%s is not a valid state abbreviation.", stateOptional.get()));
      response.setStatus(400);
      return;
    }

    String fullStateName = STATE_MAP.get(stateOptional.get());

    JSONObject deadlinesObject =
        ServletUtils.readFromApiUrl(
                String.format(BASE_URL + "/%s/%s", fullStateName, DEADLINES_PARAM),
                /* isXml= */ true)
            .get();

    JSONArray datesAndDeadlines =
        deadlinesObject
            .getJSONObject("evag")
            .getJSONObject("deadline-dates")
            .getJSONArray("deadline-date");

    JSONObject deadlines = new JSONObject();
    deadlines.put("dates", datesAndDeadlines);
    deadlines.put("state", fullStateName);

    Gson gson = new Gson();
    String json = gson.toJson(deadlines);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
