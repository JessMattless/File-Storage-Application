/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.group_9_comp20081_app;

import com.mycompany.group_9_comp20081_app.App;
import com.mycompany.group_9_comp20081_app.User;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import utils.ErrorLogger;

/**
 * FXML Controller class
 *
 * @author michael
 */
public class RegisterScreenController implements Initializable {

    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField reenterPasswordField;
    
    @FXML
    private FlowPane loginText;
    
    public RegisterScreenController(){}
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Text loginMsg = new Text("Already have an account?");
        Hyperlink loginLink = new Hyperlink("Log in");
        loginLink.setOnAction(event -> {returnToLogin();});
        loginText.getChildren().addAll(loginMsg,loginLink);
        loginText.setAlignment(Pos.CENTER);
    }
    
    @FXML
    private void returnToLogin() {
        try {
            App.setRoot("loginScreen");
        } catch (IOException e) {
            ErrorLogger.logError("Failed to load login screen", e.toString(), true);
        }
    }
    
    @FXML
    private void submit() {
        String username = usernameField.getText();
        String password_1 = passwordField.getText();
        String password_2 = reenterPasswordField.getText();
        
        UserTable ut = UserTable.getInstance();
               
        if (!password_1.equals(password_2)) {
            ErrorLogger.logWarning("Cannot register", "Passwords do not match");        
        }
        else if (password_1.length() < 6) {
            ErrorLogger.logWarning("Cannot register", "Password less than 6 characters");
        }
        else if (ut.exists(username)) {
            ErrorLogger.logWarning("Cannot register", "Username already exists");
        }
        else {
            
            User user = User.create(username, password_1);
            
            if (user != null) {
                
                App.setCurrentUser(user);
                user.generateLoginFile();
                user.generateTemporaryFile();
                
                try {
                    App.setRoot("dashboard");
                } catch (IOException e) {
                    ErrorLogger.logError("Failed to load dashboard", e.toString(), true);
                }
            }            
        }
    }
    
    @FXML
    private void keyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            submit();
        }
    }
}
