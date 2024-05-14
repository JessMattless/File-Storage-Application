/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.group_9_comp20081_app;

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
public class LoginScreenController implements Initializable {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private FlowPane registerText;
    
    public LoginScreenController(){}
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Text registerMsg = new Text("Don't have an account?");
        Hyperlink registerLink = new Hyperlink("Sign Up");
        registerLink.setOnAction(event -> {goToRegistration();});
        registerText.getChildren().addAll(registerMsg,registerLink);
        registerText.setAlignment(Pos.CENTER);
    }
    
    /**
     * Change the current scene to the registration screen
     */
    @FXML
    private void goToRegistration() {
        try {
            App.setRoot("registerScreen");
        } catch (IOException e) {
            ErrorLogger.logError("Failed to load registration screen", e.toString(), true);
        }
    }
    
    /**
     * On action event for the Login button
     * Attempt to log in using the values in the username and password fields
     */
    @FXML
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        User user = User.login(username, password);
        
        if (user == null) {
            ErrorLogger.logWarning("Unable to login", "Username or Password incorrect");
        }
        else if (User.doesTemporaryFileExist(user.getId())) {
            ErrorLogger.logWarning("Unable to Login", "User is already logged in");
            user = null;
        }
        else {
            
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
    
    @FXML
    private void keyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            login();
        }
    }
}
