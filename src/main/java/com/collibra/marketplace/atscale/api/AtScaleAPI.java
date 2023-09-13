package com.collibra.marketplace.atscale.api;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static com.collibra.marketplace.atscale.util.Constants.*;

public class AtScaleAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtScaleAPI.class);

    /***
     * utility class pattern
     * The constructor is made private to prevent the class from being instantiated from outside the class.
     */
    private AtScaleAPI() {
        throw new IllegalStateException("AtScaleAPI Utility class");
    }

    public static String apiConnect(AtScaleServerClient atScaleServerClient, String apiURL) throws UnirestException {
        atScaleServerClient.connect();
        String token = atScaleServerClient.getConnection();
        String url = atScaleServerClient.buildAPIURL(apiURL, "", "", "engine");

        HttpClient httpClient = HttpClients.custom()
                .disableCookieManagement()
                .build();
        Unirest.setHttpClient(httpClient);

        HttpResponse<String> request = Unirest.get(url)
                .header(AUTHORIZATION, BEARER+SPACE + token)
                .header(CONTENT_TYPE, APPLICATION_XML)
                .asString();

        StringBuilder inline = new StringBuilder();
        if (request.getStatus() != HttpStatus.SC_OK) {
            LOGGER.error("API call failed with error: {}", request.getStatusText());
        } else if (request.getBody().length() == 0) {
            LOGGER.error("Empty response body from API call: {}", url);
        } else {
            try (Scanner scanner = new Scanner(request.getBody())) {
                while (scanner.hasNext()) {
                    inline.append(scanner.nextLine());
                }
            }

        }
        return inline.toString();
    }

    public static List<String> getSinglePublishedProjectNames(AtScaleServerClient atScaleServerClient) {
        List<String> projectList = new ArrayList<>();

        try {
            // The PROJECTS_ENDPOINT gets the list of draft projects.
            String inline = apiConnect(atScaleServerClient, PUBLISHED_PROJECTS_ENDPOINT);

            JSONParser parse = new JSONParser();
            JSONObject dataObj = (JSONObject) parse.parse(inline);
            JSONArray projList = (JSONArray) dataObj.get("response"); // This is the list of draft projects

            for (Object obj : projList) {
                JSONObject proj = (JSONObject) obj;
                if (proj.get("linkedProjectId").toString().equals(proj.get("id").toString()) || proj.get("linkedProjectId").toString().equals("")) {
                    projectList.add(proj.get("name").toString());
                }
            }
            return projectList;


        } catch (Exception e) {
            LOGGER.error("Error while publishing project : ", e);
        }
        LOGGER.error("No published projects found via API to return");
        return Collections.emptyList();
    }
}
