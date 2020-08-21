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

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

  private static final Map<String, String> STATE_MAP;

  static {
    STATE_MAP = new HashMap<String, String>();
    STATE_MAP.put("al", "Alabama");
    STATE_MAP.put("ak", "Alaska");
    STATE_MAP.put("ab", "Alberta");
    STATE_MAP.put("az", "Arizona");
    STATE_MAP.put("ar", "Arkansas");
    STATE_MAP.put("ca", "California");
    STATE_MAP.put("co", "Colorado");
    STATE_MAP.put("ct", "Connecticut");
    STATE_MAP.put("de", "Delaware");
    STATE_MAP.put("dc", "District Of Columbia");
    STATE_MAP.put("fl", "Florida");
    STATE_MAP.put("ga", "Georgia");
    STATE_MAP.put("gu", "Guam");
    STATE_MAP.put("hi", "Hawaii");
    STATE_MAP.put("id", "Idaho");
    STATE_MAP.put("il", "Illinois");
    STATE_MAP.put("in", "Indiana");
    STATE_MAP.put("ia", "Iowa");
    STATE_MAP.put("ks", "Kansas");
    STATE_MAP.put("ky", "Kentucky");
    STATE_MAP.put("la", "Louisiana");
    STATE_MAP.put("me", "Maine");
    STATE_MAP.put("md", "Maryland");
    STATE_MAP.put("ma", "Massachusetts");
    STATE_MAP.put("mi", "Michigan");
    STATE_MAP.put("mn", "Minnesota");
    STATE_MAP.put("ms", "Mississippi");
    STATE_MAP.put("mo", "Missouri");
    STATE_MAP.put("mt", "Montana");
    STATE_MAP.put("ne", "Nebraska");
    STATE_MAP.put("nv", "Nevada");
    STATE_MAP.put("nh", "New Hampshire");
    STATE_MAP.put("nj", "New Jersey");
    STATE_MAP.put("nm", "New Mexico");
    STATE_MAP.put("ny", "New York");
    STATE_MAP.put("nc", "North Carolina");
    STATE_MAP.put("nd", "North Dakota");
    STATE_MAP.put("oh", "Ohio");
    STATE_MAP.put("ok", "Oklahoma");
    STATE_MAP.put("or", "Oregon");
    STATE_MAP.put("pa", "Pennsylvania");
    STATE_MAP.put("pr", "Puerto Rico");
    STATE_MAP.put("ri", "Rhode Island");
    STATE_MAP.put("sc", "South Carolina");
    STATE_MAP.put("sd", "South Dakota");
    STATE_MAP.put("tn", "Tennessee");
    STATE_MAP.put("tx", "Texas");
    STATE_MAP.put("ut", "Utah");
    STATE_MAP.put("vt", "Vermont");
    STATE_MAP.put("vi", "Virgin Islands");
    STATE_MAP.put("va", "Virginia");
    STATE_MAP.put("wa", "Washington");
    STATE_MAP.put("wv", "West Virginia");
    STATE_MAP.put("wi", "Wisconsin");
    STATE_MAP.put("wy", "Wyoming");
  }

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

    String fullStateName = STATE_MAP.get(stateOptional.get());

    JSONObject deadlinesObject =
        ServletUtils.readFromApiUrl(
                String.format(BASE_URL + "/%s/%s", fullStateName, DEADLINES_PARAM))
            .get();

    JSONArray datesAndDeadlines =
        deadlinesObject
            .getJSONObject("evag")
            .getJSONObject("deadline-dates")
            .getJSONArray("deadline-date");

    Gson gson = new Gson();
    String json = gson.toJson(datesAndDeadlines);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
