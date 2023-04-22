package com.example.deepleaningclient;

import java.io.IOException;
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

public class SignupController {
    @FXML
    private Label signupText;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;

    @FXML
    public void toLogin(ActionEvent event) throws IOException {
        Parent signupViewParent = FXMLLoader.load(getClass().getResource("login-view.fxml"));
        Scene signupViewScene = new Scene(signupViewParent, 800, 600);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(signupViewScene);
        window.show();
    }
    @FXML
    protected void signupClick(ActionEvent event) {
        String username = this.usernameField.getText();
        String password = this.passwordField.getText();

        try {
            String[][] header = new String[2][2];
            header[0][0] = "username";
            header[0][1] = username;
            header[1][0] = "password";
            header[1][1] = password;
            String response = Server.APIrequest("/signup", "POST", header);

            if (response.equals("200")) {
                signupText.setText("Le compte " + username  + " a été créé !");
            } else if (response.equals("400")) {
                signupText.setText("Le nom d'utilisateur et le mot de passe doivent avoir 3 caractères ou plus");
            }
            else if (response.equals("409")) {
                signupText.setText("Le compte " + username  + " existe déjà");
            } else {
                signupText.setText("Erreur lors de la création du compte !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void quitClick(ActionEvent event) {
        Scene scene = ((Node) event.getSource()).getScene();
        Stage stage = (Stage) scene.getWindow();
        stage.close();
    }
}
