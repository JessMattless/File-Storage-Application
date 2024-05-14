/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.group_9_comp20081_app;

import database.FileTable;
import database.SharedFileTable;
import database.UserTable;
import filesystem.StoredFile;
import java.net.URL;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import security.EncryptDecryptException;
import security.RSA;
import utils.ErrorLogger;
/**
 * FXML Controller class
 *
 * @author michael
 */
public class UserSelectModal implements Initializable {
    private StoredFile storedFile;
    @FXML
    private TextField userSearchBar;
    @FXML
    private Button searchBtn;
    @FXML
    private TableView<UserModel> userTable;
    @FXML
    private TableColumn<UserModel, Integer> userIdCol;
    @FXML
    private TableColumn<UserModel, String> userNameCol;
    @FXML
    private TableColumn<UserModel, CheckBox> shareCol;
    @FXML
    private TableColumn<UserModel, CheckBox> readOnlyCol;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        shareCol.setCellValueFactory(new PropertyValueFactory<>("shared"));
        readOnlyCol.setCellValueFactory(new PropertyValueFactory<>("readOnly"));
        userTable.setItems(listUsers());
    }
    
    
    private ObservableList<UserModel> listUsers() {
        ArrayList<UserModel> users = UserTable.getInstance().getUsers();
        ObservableList<UserModel> userModels = FXCollections.observableArrayList(users);
        return userModels;
    }
    
    private ObservableList<UserModel> listUsers(String filter) {
        ArrayList<UserModel> users = UserTable.getInstance().getUsers(filter);
        ObservableList<UserModel> userModels = FXCollections.observableArrayList(users);
        return userModels;
    }
    
    @FXML
    private void filterList() {
        String filter = userSearchBar.getText();
        userTable.refresh();
        userTable.setItems(listUsers(filter));
    }
    
    public void linkFile(StoredFile storedFile) {
        this.storedFile = storedFile;
    }

    @FXML
    private void submit(ActionEvent event) {
        try {
            byte[] encryptedKey = FileTable.getInstance().loadAesKey(storedFile.getId());
            byte[] fileKey = RSA.decryptBytes(encryptedKey, App.getCurrentUser().getPrivateKey());

            for(UserModel userModel: userTable.getItems()){
                if (userModel.getIsShared()) {
                    byte[] sharedUserPublicKeyBytes = UserTable.getInstance().loadPublicKey(userModel.getId());
                    PublicKey sharedUserPublicKey = RSA.publicKeyFromBytes(sharedUserPublicKeyBytes);
                    byte[] sharedUserFileKey = RSA.encryptBytes(fileKey, sharedUserPublicKey);

                    SharedFileTable.getInstance().insert(userModel.getId(), storedFile.getId(), !userModel.getIsReadOnly(), sharedUserFileKey);
                }
            }
            Node node = (Node) event.getSource();
            Stage window = (Stage) node.getScene().getWindow();
            window.close();
        }
        catch (EncryptDecryptException e) {
            ErrorLogger.logError("Error encrypting shared file", e.toString(), false);
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        Node node = (Node) event.getSource();
        Stage window = (Stage) node.getScene().getWindow();
        window.close();
    }
}
