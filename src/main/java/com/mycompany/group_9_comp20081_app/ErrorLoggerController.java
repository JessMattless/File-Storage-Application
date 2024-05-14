/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.group_9_comp20081_app;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import utils.ErrorLogger;
import utils.FileHandler;

/**
 * FXML Controller class
 *
 * @author jakem
 */
public class ErrorLoggerController implements Initializable {

    @FXML
    TextArea logArea;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<String> logText = FileHandler.readFile("error.log");
        
        for (String line : logText) {
            logArea.appendText(line + "\n");
        }
    }
    
    @FXML
    private void returnToDashboard() {
        try {
            App.setRoot("dashboard");
        } catch (IOException e) {
            ErrorLogger.logError("Failed to load dashboard", e.toString(), true);
        }
    }
    
}
