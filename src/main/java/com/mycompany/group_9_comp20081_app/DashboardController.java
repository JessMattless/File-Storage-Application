/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.group_9_comp20081_app;

import filesystem.Directory;
import dashboard.FileViewer;
import dashboard.FolderTree;
import filesystem.StoredFile;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.ErrorLogger;

/**
 * FXML Controller class
 *
 * @author michael
 */
public class DashboardController implements Initializable {

    @FXML 
    private ScrollPane folderTreePane;
    
    @FXML 
    private FolderTree folderTree;
    
    @FXML
    private ScrollPane fileViewContainer;
    
    @FXML
    private FileViewer fileViewer;
    
    @FXML
    private TextField pathField;
    
    @FXML
    private Button newDirBtn, newFileBtn, uploadBtn, userSettings, consoleBtn, logout, backBtn, forwardBtn, upBtn, errorLoggerBtn;
    
    public DashboardController(){}
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fileViewer = new FileViewer(this);
        fileViewContainer.setContent(fileViewer);
        folderTree = new FolderTree(this);
        folderTreePane.setContent(folderTree);
        try {
            newDirBtn.setGraphic(getIcon("file:src/main/resources/images/add-folder.png", 0));
            newFileBtn.setGraphic(getIcon("file:src/main/resources/images/add-file.png", 0));
            uploadBtn.setGraphic(getIcon("file:src/main/resources/images/file-upload.png", 0));
            userSettings.setGraphic(getIcon("file:src/main/resources/images/user-settings.png", 0));
            consoleBtn.setGraphic(getIcon("file:src/main/resources/images/terminals.png", 0));
            errorLoggerBtn.setGraphic(getIcon("file:src/main/resources/images/error.png", 0));
            logout.setGraphic(getIcon("file:src/main/resources/images/logout.png", 0));
            backBtn.setGraphic(getIcon("file:src/main/resources/images/arrow.png", 0));
            forwardBtn.setGraphic(getIcon("file:src/main/resources/images/arrow.png", 180));
            upBtn.setGraphic(getIcon("file:src/main/resources/images/arrow.png", 90));
        } catch (IllegalArgumentException e) {
            ErrorLogger.logError("Failed to initialise dashboard", e.toString(), true);
        }
        
        User user = App.getCurrentUser();
        if (user.getPrivileges() == 0) {
            errorLoggerBtn.setVisible(false);
        }
    }
    
    @FXML
    private void logout() {
        User currentUser = App.getCurrentUser();
        currentUser.deleteLoginFile();
        currentUser.deleteTempFileIfExists();
        App.setCurrentUser(null);
        try {
            App.setRoot("loginScreen");
        } catch (IOException e) {
            ErrorLogger.logError("Failed to load login screen", e.toString(), true);
        }
    }
    
    @FXML
    private void goToSettings() {
        try {
            App.setRoot("userSettings");
        } catch (IOException e) {
            ErrorLogger.logError("Failed to load user settings", e.toString(), true);
        }
    }
    
    @FXML
    private void goToConsole() {
        try {
            App.setRoot("console");
        } catch (IOException e) {
            ErrorLogger.logError("Failed to load console", e.toString(), true);
        }
    }
    
    @FXML
    private void goToErrorLogger() {
        try {
            App.setRoot("errorLogger");
        }
        catch (IOException e) {
            ErrorLogger.logError("Failed to load error logger page", e.toString(), true);
        }
    }
    
    @FXML
    private void uploadFile() {
        Directory currentDir = this.fileViewer.getCurrentDir();
        Stage stage = new Stage();
        
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showOpenDialog(stage);
        if (selectedFile != null) {
            StoredFile storedFile = StoredFile.store(selectedFile, currentDir.getId());
            fileViewer.add(storedFile);
        }
    }   
    
    @FXML
    private void createDirectory() {
        this.fileViewer.createDirectory();
    }
    
    public void changeFileViewDir(Directory dir) {     
        fileViewer.changeDirs(dir);
    }
    
    @FXML
    public void createFile() {
        this.fileViewer.createFile();
    }
    
    /**
     * Enable or disable certain controls depending on which drive we are in
     * @param driveId the root directory id
     */
    public void updateControls(int driveId) {
        if (driveId == 0) {
            uploadBtn.setDisable(false);
            newFileBtn.setDisable(false);
            newDirBtn.setDisable(false);
        }
        else {
            uploadBtn.setDisable(true);
            newFileBtn.setDisable(true);
            newDirBtn.setDisable(true);
        }
    }

    @FXML
    private void navigateBack() {
        fileViewer.navigateBack();
    }
    
    @FXML
    private void navigateForward() {
        fileViewer.navigateForward();
    }
    
    @FXML
    private void navigateParent() {
        fileViewer.navigateParent();
    }
    
    /**
     * Get an icon for the control buttons.
     * @param url
     * @param rotation
     * @return ImageView
     */
    private ImageView getIcon(String url, double rotation) {
        Image image = new Image(url, 20, 0, true, true);
        ImageView imageView = new ImageView(image);
        imageView.rotateProperty().set(rotation);
        return imageView;
    }
    
    /**
     * Updates the textfield that displays the file path to the current dir
     * @param path 
     */
    public void updatePathField(String path) {
        pathField.clear();
        pathField.setText(path);
    }

    
    public void updateFolderTree() {
        folderTree.refreshTree();
    }
    
    public void addToTree(Directory parent, Directory newDir) {
        folderTree.addDirToTree(parent, newDir);
    }
    
    public void removeFromTree(Directory removeDir) {
        folderTree.removeDirFromTree(removeDir);
    }
}
