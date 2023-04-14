package com.example.deepleaningclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Server {
    private static final String url = "http://localhost:3000";
    public static String APIrequest(String endpoint, String type, String[][] header){
        try {
            URL url = new URL(Server.url + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(type);
            conn.setRequestProperty("Accept", "application/json");

            for (int i = 0; i < header.length; i++) {
                conn.setRequestProperty(header[i][0], header[i][1]);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String response = br.readLine().trim();
            conn.disconnect();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "401";
        }
    }
}
