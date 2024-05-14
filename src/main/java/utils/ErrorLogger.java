/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import com.mycompany.group_9_comp20081_app.App;
import com.mycompany.group_9_comp20081_app.User;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 *
 * @author jakem
 */
public class ErrorLogger {
    public static boolean logYesNo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.getButtonTypes().removeAll(ButtonType.CANCEL, ButtonType.OK);
        alert.getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);

        alert.showAndWait();
        
        if (alert.getResult() == ButtonType.YES)
            return true;
        
        return false;
    }
    
    public static void logConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(title);
        alert.setContentText(content);
//        Optional<ButtonType> result = alert.showAndWait();
        alert.show();
    }
    
    public static void logError(String message, String error, boolean showUser) {
        String errorMessage = message + ": " + error;
        User user = App.getCurrentUser();
        Date currentDate = new Date();
        String extendedErrorMessage = "[" + currentDate + "] - User: " + user.getId() + " | " + user.getName() + " - " + errorMessage + "\n";
        
        FileHandler.appendFile("error.log", extendedErrorMessage);
        
        if (showUser) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(errorMessage);
            alert.show();
        }
    }
    
    public static void logWarning(String message, String warning) {
        String errorMessage = message + ": " + warning;
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(errorMessage);
        alert.show();
    }
}
