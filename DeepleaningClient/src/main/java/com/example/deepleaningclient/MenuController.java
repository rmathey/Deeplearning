package com.example.deepleaningclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;



import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Node;

public class MenuController {
    @FXML
    private TableView<Mot> tableView;
    @FXML
    private TableColumn<Mot, String> colonneMot;
    @FXML
    private TableColumn<Mot, String> colonneTraduction;
    @FXML
    private Label usernameText;
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
        Scene signupViewScene = new Scene(signupViewParent, 800, 600);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(signupViewScene);
        window.show();
    }

    @FXML
    public void reload() throws IOException{
        String response = this.checkToken();
        if (response.equals("200")) {
            this.user = new User(this.jwt);
            usernameText.setText("Bienvenue " + this.user.getUsername());
            this.fillTableView();
        }
    }

    @FXML
    public void update() throws IOException {
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
    public void add() throws IOException {
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
    public void delete() throws IOException {
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
    public void reset() throws IOException {
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

    private void fillTableView() {
        ObservableList<Mot> listeMots = FXCollections.observableArrayList();
        String [][] mots = this.user.getListe();
        Mot mot;
        for (int i = 0; i < mots.length; i++) {
            mot = new Mot(mots[i][0], mots[i][1]);
            listeMots.addAll(mot);
        }
        this.colonneMot.setCellValueFactory(new PropertyValueFactory<>("mot"));
        this.colonneTraduction.setCellValueFactory(new PropertyValueFactory<>("traduction"));
        this.tableView.setItems(listeMots);
        this.tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public String checkToken() throws IOException {
        String[][] header = new String[1][2];
        header[0][0] = "jwt";
        header[0][1] = this.jwt;
        String response = Server.APIrequest("/checkToken", "GET", header);
        if (!response.equals("200")) {
            this.jwtExpired();
        }
        return response;
    }
    public void jwtExpired() throws IOException {
        this.setJwt(null);
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText("La connexion a expirée, veuillez vous reconnecter");

        ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(buttonTypeOk);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeOk){
            this.setJwt(null);
            Stage stage = (Stage) menuInfoText.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
        }
    }
    public static class Mot {
        private final String mot;
        private final String traduction;

        public Mot(String mot, String traduction) {
            this.mot = mot;
            this.traduction = traduction;
        }

        public String getMot() {
            return mot;
        }

        public String getTraduction() {
            return traduction;
        }
    }
}
