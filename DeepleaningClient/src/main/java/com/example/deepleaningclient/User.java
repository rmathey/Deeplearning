package com.example.deepleaningclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class User {
    private static User instance;
    public String _id;
    public String username;
    public String password;
    public String[][] liste;

    private User() {}
    public static User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }
    public void reload(String jwt) {
        try {
            String jsonStr = getInfo(jwt);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonStr);

            this._id = jsonNode.get("_id").asText();
            this.username = jsonNode.get("username").asText();
            this.password = jsonNode.get("password").asText();

            JsonNode listeNode = jsonNode.get("liste");
            if (listeNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) listeNode;
                this.liste = new String[arrayNode.size()][2];
                for (int i = 0; i < arrayNode.size(); i++) {
                    JsonNode innerNode = arrayNode.get(i);
                    if (innerNode.isObject()) {
                        ObjectNode innerObjNode = (ObjectNode) innerNode;
                        int finalI = i;
                        innerObjNode.fields().forEachRemaining(entry -> {
                            String key = entry.getKey();
                            String value = entry.getValue().asText();
                            this.liste[finalI][0] = key;
                            this.liste[finalI][1] = value;
                        });
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {return this.username;}

    public String[][] getListe() {return this.liste;}

    private String getInfo(String jwt) {
        String[][] header = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        return Server.getInstance().APIrequest("/getInfo", "GET", header);
    }
    public String update(String jwt) throws IOException {
        String[][] header = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        return Server.getInstance().APIrequest("/update", "PUT", header);
    }
    public String add(String jwt, String word) {
        String[][] header = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        return Server.getInstance().APIrequest("/addWord?word=" + word, "PUT", header);
    }
    public String delete(String jwt, String word) {
        String[][] header = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        return Server.getInstance().APIrequest("/delWord?word=" + word, "PUT", header);
    }
    public String reset(String jwt) {
        String[][] header = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        return Server.getInstance().APIrequest("/emptyList", "PUT", header);
    }
}
