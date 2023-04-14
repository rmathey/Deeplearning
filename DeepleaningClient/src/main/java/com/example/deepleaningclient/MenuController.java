package com.example.deepleaningclient;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Node;

public class MenuController {
    @FXML
    private Label usernameText;
    @FXML
    private Label listeText;
    @FXML
    private TextField addWordField;
    @FXML
    private TextField deleteWordField;
    private String jwt;
    private User user;

    @FXML
    public void reload() {
        this.user = new User(this.jwt);
        usernameText.setText(this.user.getUsername());
        listeText.setText(this.user.getListe());
    }

    @FXML
    public void update() {
        String header[][] = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        Server.APIrequest("/update", "PUT", header);
        this.reload();
    }

    @FXML
    public void add() {
        String header[][] = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        Server.APIrequest("/addWord?word=" + this.addWordField.getText(), "PUT", header);
        this.reload();
    }

    @FXML
    public void delete() {
        String header[][] = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        Server.APIrequest("/delWord?word=" + this.deleteWordField.getText(), "PUT", header);
        this.reload();
    }

    @FXML
    public void reset() {
        String header[][] = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        Server.APIrequest("/emptyList", "PUT", header);
        this.reload();
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}
