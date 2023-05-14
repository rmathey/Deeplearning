package com.example.deepleaningclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Server {
    private static final String url = "http://localhost:3000";
    private static Server instance = null;
    private Server() {}
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }
    public String APIrequest(String endpoint, String type, String[][] header){
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
    public boolean checkJWT(String jwt) {
        try {
            String[][] header = new String[1][2];
            header[0][0] = "jwt";
            header[0][1] = jwt;
            String response = this.APIrequest("/checkToken", "GET", header);
            char codeRetour = response.charAt(0);
            return codeRetour != '4';

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public String signin(String username, String password) {
        try {
            String header[][] = new String[2][2];
            header[0][0] = "username";
            header[0][1] = username;
            header[1][0] = "password";
            header[1][1] = password;
            return Server.getInstance().APIrequest("/signin", "POST", header);
        } catch (Exception e) {
            e.printStackTrace();
            return "500";
        }
    }
    public String signup(String username, String password) {
        try {
            String[][] header = new String[2][2];
            header[0][0] = "username";
            header[0][1] = username;
            header[1][0] = "password";
            header[1][1] = password;
            return Server.getInstance().APIrequest("/signup", "POST", header);
        } catch (Exception e) {
            e.printStackTrace();
            return "500";
        }
    }
}
