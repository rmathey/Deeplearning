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

public class LoginController {
    @FXML
    private String jwt;
    @FXML
    private Label loginText;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;

    @FXML
    public void toSignup(ActionEvent event) throws IOException {
        Parent signupViewParent = FXMLLoader.load(getClass().getResource("signup-view.fxml"));
        Scene signupViewScene = new Scene(signupViewParent, 800, 600);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(signupViewScene);
        window.show();
    }

    @FXML
    protected void loginClick(ActionEvent event) {
        String username = this.usernameField.getText();
        String password = this.passwordField.getText();

        try {
            String header[][] = new String[2][2];
            header[0][0] = "username";
            header[0][1] = username;
            header[1][0] = "password";
            header[1][1] = password;
            String response = Server.APIrequest("/signin", "GET", header);
            boolean connected = this.checkJWT(response);

            if (connected) {
                this.jwt = response;
                loginText.setText("");
                this.toMenu(event, "menu-view.fxml");
            }
            else {
                loginText.setText("Username ou mot de passe incorrecte");
            }
        } catch (Exception e) {
            loginText.setText("Erreur inconnue");
            e.printStackTrace();
        }
    }

    @FXML
    public boolean checkJWT(String jwt) {
        try {
            String header[][] = new String[1][2];
            header[0][0] = "jwt";
            header[0][1] = jwt;
            String response = Server.APIrequest("/checkToken", "GET", header);
            char codeRetour = response.charAt(0);
            ;
            return codeRetour != '4';

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @FXML
    protected void quitClick(ActionEvent event) {
        Scene scene = ((Node) event.getSource()).getScene();
        Stage stage = (Stage) scene.getWindow();
        stage.close();
    }
    public void toMenu(ActionEvent event, String sceneName){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(sceneName));
            Parent root = loader.load();
            MenuController controller = loader.getController();
            controller.setJwt(this.jwt);
            controller.reload();
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
