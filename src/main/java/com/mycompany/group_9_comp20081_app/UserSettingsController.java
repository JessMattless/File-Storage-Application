/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.group_9_comp20081_app;

import database.UserTable;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.ErrorLogger;

/**
 * FXML Controller class
 *
 * @author michaels
 */
public class UserSettingsController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField currentPasswordField, newPasswordField1, newPasswordField2;
    @FXML
    private Button changeUsername, usernameSave, usernameCancel, changePassword, passwordSave, passwordCancel;
    
    public UserSettingsController(){}
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Scene scene = App.getScene();
        
        User user = App.getCurrentUser();
        
        usernameField.setText(user.getName());
        currentPasswordField.setPromptText("");
        newPasswordField1.setPromptText("");
        newPasswordField2.setPromptText("");
    }
    
    @FXML
    private void toDashboard() {
        try {
            App.setRoot("dashboard");
        } catch (IOException e) {
            ErrorLogger.logError("Failed to load dashboard", e.toString(), true);
        }
    }
    
    @FXML
    private void updatePassword() {
        String password = currentPasswordField.getText();
        String newPass_1 = newPasswordField1.getText();
        String newPass_2 = newPasswordField2.getText();

        if (!newPass_1.equals(newPass_2)) {
            ErrorLogger.logWarning("Error updating password", "New passwords do not match");
        }
        else if (newPass_1.length() < 6) {
            ErrorLogger.logWarning("Error updating password", "New password must be at least 6 characters");
        }
        else {
            User user = App.getCurrentUser();
            try {
                if (User.load(user.getName(), password) != null) {
                    boolean alertResult = ErrorLogger.logYesNo("Are you Sure?", "Are you sure you want to change your password?");
                    
                    if (alertResult) {
                        user.updatePassword(newPass_1);
                        cancelPassword();
                    }
                }
            }
            catch (NullPointerException ex){
                ErrorLogger.logWarning("Problem with password", "The current password is incorrect");
            }
        }
    }
    
    @FXML
    private void editPassword() {
        changePassword.setVisible(false);
        passwordSave.setVisible(true);
        passwordCancel.setVisible(true);
        
        currentPasswordField.setPromptText("Enter Old Password");
        currentPasswordField.setDisable(false);
        currentPasswordField.setEditable(true);
        
        newPasswordField1.setPromptText("Enter New Password");
        newPasswordField1.setDisable(false);
        newPasswordField1.setEditable(true);
        
        newPasswordField2.setPromptText("Re-enter New Password");
        newPasswordField2.setDisable(false);
        newPasswordField2.setEditable(true);
    }
    
    @FXML
    private void cancelPassword() {        
        changePassword.setVisible(true);
        passwordSave.setVisible(false);
        passwordCancel.setVisible(false);
        
        currentPasswordField.setPromptText("");
        currentPasswordField.setText("");
        currentPasswordField.setDisable(true);
        currentPasswordField.setEditable(false);
        
        newPasswordField1.setPromptText("");
        newPasswordField1.setText("");
        newPasswordField1.setDisable(true);
        newPasswordField1.setEditable(false);
        
        newPasswordField2.setPromptText("");
        newPasswordField2.setText("");
        newPasswordField2.setDisable(true);
        newPasswordField2.setEditable(false);
    }
    
    @FXML
    private void updateUsername() {                  
        String username = usernameField.getText();
            
        UserTable ut = UserTable.getInstance();


        if (username.length() == 0) {
            ErrorLogger.logWarning("Problem with username", "Username field cannot be empty");
        }
        else if (ut.exists(username)) {
            ErrorLogger.logWarning("Problem with username", "New username already exists");
        }
        else {
            boolean alertResult = ErrorLogger.logYesNo("Are you sure?", "Are you sure you want to change your username?");
            
            if (alertResult) {
                User user = App.getCurrentUser();
                user.updateName(username);
                cancelUsername();
            }
        }
    }
    
    @FXML
    private void editUsername() {
        changeUsername.setVisible(false);
        usernameSave.setVisible(true);
        usernameCancel.setVisible(true);
        
        usernameField.setDisable(false);
        usernameField.setEditable(true);
    }
    
    @FXML
    private void cancelUsername() {
        User user = App.getCurrentUser();
        
        usernameField.setText(user.getName());
        
        changeUsername.setVisible(true);
        usernameSave.setVisible(false);
        usernameCancel.setVisible(false);
        
        usernameField.setDisable(true);
        usernameField.setEditable(false);
    }
    
    @FXML
    private void deleteUser() {
        boolean alertResult = ErrorLogger.logYesNo("Are you Sure?", "Are you sure you want to delete your account?");
        
        if (alertResult) {
            boolean alertConfirmation = ErrorLogger.logYesNo("Are you Sure?", "Are you absolutely sure?");
            
            if (alertConfirmation) {
                User user = App.getCurrentUser();
                App.setCurrentUser(null);
                user.delete();
                try {
                    App.setRoot("loginScreen");
                }
                catch (IOException e) {
                    ErrorLogger.logError("Failed to load login screen", e.toString(), true);
                }
            }
        }
    }
    
}
