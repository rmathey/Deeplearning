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

public class MenuController {
    @FXML
    private Label usernameText;
    @FXML
    private Label listeText;
    @FXML
    private Label menuInfoText;
    @FXML
    private TextField addWordField;
    @FXML
    private TextField deleteWordField;
    private String jwt;
    private User user;

    @FXML
    public void toLogin(ActionEvent event) throws IOException {
        Parent signupViewParent = FXMLLoader.load(getClass().getResource("login-view.fxml"));
        Scene signupViewScene = new Scene(signupViewParent, 1200, 800);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(signupViewScene);
        window.show();
    }

    @FXML
    public void reload() {
        this.user = new User(this.jwt);
        usernameText.setText(this.user.getUsername());
        listeText.setText(this.user.getListe());
    }

    @FXML
    public void update() {
        String[][] header = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        String response = Server.APIrequest("/update", "PUT", header);
        if (response.equals("200")) {
            menuInfoText.setText("La liste de vocabulaire a été mise à jour");
        }
        else {
            menuInfoText.setText("Une erreur est survenue lors de la mise à jour de la liste de vocabulaire");
        }
        this.reload();
    }

    @FXML
    public void add() {
        String[][] header = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        String response = Server.APIrequest("/addWord?word=" + this.addWordField.getText(), "PUT", header);
        if (response.equals("200")) {
            menuInfoText.setText("Le mot " + this.addWordField.getText() + " a été ajouté à la liste de vocabulaire");
            this.addWordField.setText("");
        }
        else if (response.equals("400")) {
            menuInfoText.setText("Veuillez entrer un mot à ajouter");
        }
        else if (response.equals("409")) {
            menuInfoText.setText("Le mot " + this.addWordField.getText() + " est déja dans la liste");
        }
        else {
            menuInfoText.setText("Erreur lors de l'ajout du mot " + this.addWordField.getText());
        }
        this.reload();
    }

    @FXML
    public void delete() {
        String[][] header = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        String response = Server.APIrequest("/delWord?word=" + this.deleteWordField.getText(), "PUT", header);
        if (response.equals("200")) {
            menuInfoText.setText("Le mot " + this.deleteWordField.getText() + " a été supprimé de la liste de vocabulaire");
            this.deleteWordField.setText("");
        }
        else if (response.equals("400")) {
            menuInfoText.setText("Veuillez entrer un mot à supprimer");
        }
        else if (response.equals("409")) {
            menuInfoText.setText("Le mot " + this.deleteWordField.getText() + " n'est pas dans la liste");
        }
        else {
            menuInfoText.setText("Erreur lors de la suppression du mot " + this.deleteWordField.getText());
        }
        this.reload();
    }

    @FXML
    public void reset() {
        String[][] header = new String[1][2];
        header[0][0] = "token";
        header[0][1] = jwt;
        String response = Server.APIrequest("/emptyList", "PUT", header);
        if (response.equals("200")) {
            menuInfoText.setText("La liste de vocabulaire a bien été réinitialisée !");
        }
        else {
            menuInfoText.setText("Erreur lors de la réinitialisation de la liste de vocabulaire");
        }
        this.reload();
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}
