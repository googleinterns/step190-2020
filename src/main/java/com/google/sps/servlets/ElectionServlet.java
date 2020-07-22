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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

@WebServlet("/election")
public class ElectionServlet extends HttpServlet {

  private static final String baseURL = "https://www.googleapis.com/civicinfo/v2/elections?key=";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder strBuf = new StringBuilder();  
        
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {  
            URL url = new URL(baseURL + "AIzaSyBlNN_uUrbqY2uRaBpKTD-5Jbw6nk8n0_k");
            conn = (HttpURLConnection) url.openConnection();  

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("HTTP GET Request Failed with Error code : "
                              + conn.getResponseCode());
            }
            
            // Read the content from the defined connection
            // Using IO Stream with Buffer for increased efficiency 
	        reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
            String output = null;  

            while ((output = reader.readLine()) != null) {
                strBuf.append(output);
            }                 
        } catch(MalformedURLException e) {
            response.setContentType("text/html");
            response.getWriter().println("URL is incorrectly formatted");
            return;
        } catch(IOException e) {  
            response.setContentType("text/html");
            response.getWriter().println("Cannot retrieve information from provided URL");
            return;     
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    response.setContentType("text/html");
                    response.getWriter().println("Could not read contents found at URL");
                    return;
                }
            } 
            if(conn != null) {
                conn.disconnect();
            }
        }

        String elections = strBuf.toString();  
        Gson gson = new Gson();
        String json = gson.toJson(elections);

        response.setContentType("application/json;");
        response.getWriter().println(json);
    }
}
