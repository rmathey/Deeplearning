package com.example.deepleaningclient;

import java.io.BufferedReader;
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
    public String _id;
    public String username;
    public String password;
    public String[][] liste;

    public User(String jwt) {
        try {
            String jsonStr = this.getInfo(jwt);
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
                        //int finalI = i;
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

    public String getUsername() {
        return this.username;
    }

    public String getListe() {
        String res = "";
        for (int i = 0; i < this.liste.length; i++) {
            res += this.liste[i][0];
            res += " = ";
            res += this.liste[i][1];
            res += "\n";
        }
        return res;
    }

    private String getInfo(String jwt) {
        String header[][] = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        return Server.APIrequest("/getInfo", "GET", header);
    }
}
