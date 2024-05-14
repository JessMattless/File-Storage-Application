/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dashboard;

import customContextMenu.FileViewContextMenu;
import com.mycompany.group_9_comp20081_app.App;
import filesystem.Directory;
import filesystem.StoredFile;
import com.mycompany.group_9_comp20081_app.User;
import com.mycompany.group_9_comp20081_app.DashboardController;
import filesystem.SharedFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.ErrorLogger;
import utils.FileHandler;

/**
 *
 * @author michael
 */
public class FileViewer extends FlowPane {
    private Directory currentDir;
    FileViewContextMenu contextMenu;
    DashboardController userDashboard;
    private ArrayList<Directory> history = new ArrayList<>();
    private int historyIndex = 1;
    
    /**
     * This constructor is used if no directory is passed as an argument <br>
     * Will set the directory as root and call the main constructor
     * @param userDashboard 
     */
    public FileViewer(DashboardController userDashboard) {
        // if no directory is passed as argument then current dir is root
        this(Directory.root(), userDashboard);
    }
    
    /**
     * Main constructor
     * @param currentDir
     * @param userDashboard 
     */
    public FileViewer(Directory currentDir, DashboardController userDashboard) {
        // store the dashboard instance so we can update the path field when directories change
        this.userDashboard = userDashboard;
        userDashboard.updatePathField(currentDir.getPath());
        this.currentDir = currentDir;
        // add the current directory as the first item in the history list
        history.add(currentDir);
        this.setPadding(new Insets(10));
        populateList();
        
        contextMenu = new FileViewContextMenu(this);
        
        // use this context menu when context menu requested
        FlowPane pane = this;        
        pane.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>(){ 
            public void handle(ContextMenuEvent e) {
               contextMenu.show(pane.getScene().getWindow(), e.getScreenX(), e.getScreenY()); 
            }
        });
    }
    
    /**
     * Show list of directories and files in the viewing window
     */
    private void populateList() {
        User user = App.getCurrentUser();
        ArrayList<Directory> userDirs = user.getDirs(currentDir);
        ArrayList<StoredFile> userFiles = user.getFiles(currentDir.getId()); 
        ArrayList<SharedFile> sharedFiles = user.getSharedFiles(currentDir.getId()); 

        for (Directory userDir : userDirs) {
            add(userDir);
        }

        for (StoredFile storedFile : userFiles) {
            add(storedFile);
        }

        for (SharedFile sharedFile : sharedFiles) {
            add(sharedFile);
        }
    }
    
    /**
     * Create a FileIcon object for this directory and add it to the viewing window
     * @param fileObject 
     */
    public void add(Directory fileObject) {
        FileIcon fileIcon = new FileIcon(fileObject, currentDir);
        this.getChildren().add(fileIcon);
    }
    
    /**
     * Create a FileIcon object for this file and add it to the viewing window
     * @param fileObject 
     */
    public void add(StoredFile fileObject) {
        FileIcon fileIcon = new FileIcon(fileObject, currentDir);
        this.getChildren().add(fileIcon);
    }
    
    /**
     * Create a FileIcon object for this file and add it to the viewing window
     * @param fileObject 
     */
    public void add(SharedFile fileObject) {
        FileIcon fileIcon = new FileIcon(fileObject, currentDir);
        this.getChildren().add(fileIcon);
    }
    
    /**
     * Change the current directory. <br>
     * Clears and shows directories and files for given directory.
     * @param dir 
     */
    public void changeDirs(Directory dir) {
        if (dir != currentDir) {
            currentDir = dir;
            refreshList();
            history = new ArrayList<Directory>( history.subList(0, historyIndex) );
            history.add(dir);
            historyIndex++;
        }
    }
    
    /**
     * Clear all icons from the viewing window and call populateList()
     */
    public void refreshList(){
        this.getChildren().clear();
        userDashboard.updatePathField(currentDir.getPath());
        userDashboard.updateControls(currentDir.getRoot().getId());
        populateList();
        contextMenu.refresh();
    }
    
    /**
     * @brief refreshes the folder tree view
     */
    public void refreshTreeView() {
        userDashboard.updateFolderTree();
    }
    
    /**
     * Get the current directory
     * @return Directory object
     */
    public Directory getCurrentDir() {
        return currentDir;
    }
    
    /**
     * Change directory to the previously visited one using the history list
     */
    public void navigateBack() {
        if (historyIndex > 1) {
            currentDir = history.get(--historyIndex - 1);
            refreshList();
        }
    }
    
    /**
     * Navigate forward to directory. Only available after using back. <br>
     * All forward history is deleted when a directory is selected.
     */
    public void navigateForward() {
        if (historyIndex < history.size()) {
            currentDir = history.get(++historyIndex - 1);
            refreshList();
        }
    }
    
    /**
     * Navigate to this directories parent directory
     */
    public void navigateParent() {
        if (currentDir.getParent() != null) {
            changeDirs(currentDir.getParent());
        } 
    }
    
    /**
     * Create a new directory in the current directory. <br>
     * Directory icon is created and data is persisted into the database
     */
    public void createDirectory() {        
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Create New Directory");
 
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(30);
        grid.setPadding(new Insets(20, 20, 20, 20));

        Label fileNameLabel = new Label("Enter Directory Name: ");
        fileNameLabel.setStyle("-fx-font: 16 arial;");
        TextField fileName = new TextField();
        fileName.setPromptText("Directory Name");
        fileName.setPrefHeight(40);
        
        HBox spacer = new HBox();

        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("cancelButton");
        
        Button submit = new Button("Save");
        
        // cancel button action
        cancel.setOnAction(e -> {
            stage.close();
        });
        
        // submit button action
        submit.setOnAction(e -> {
            String newDirName = fileName.getText();
            Directory dir = Directory.create(newDirName, currentDir);
            this.add(dir);
            userDashboard.addToTree(currentDir,dir);
            stage.close();
        });
        
        ColumnConstraints col1 = new ColumnConstraints(75, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        ColumnConstraints col2 = new ColumnConstraints(100, USE_COMPUTED_SIZE, Double.MAX_VALUE);
        ColumnConstraints col3 = new ColumnConstraints(75, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        
        grid.getColumnConstraints().addAll(col1, col2, col3);

        grid.add(fileNameLabel, 0, 0, 3, 1);
        grid.add(fileName, 0, 1, 3, 1);

        grid.add(submit, 0, 2);
        grid.add(spacer, 1, 2);
        grid.add(cancel, 2,2);

        Scene scene = new Scene(grid, 300, 200, Color.DARKGRAY);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();      
    }
    
    public void createFile() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Create New File");
 
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 20, 20));

        Label fileNameLabel = new Label("Enter File Name: ");
        fileNameLabel.setStyle("-fx-font: 16 arial;");
        TextField fileName = new TextField();
        fileName.setPromptText("File Name");
        fileName.setMinHeight(40);
        
        Label fileContentLabel = new Label("Enter File Content: ");
        fileContentLabel.setStyle("-fx-font: 16 arial");
        TextArea editArea = new TextArea();
        editArea.setPromptText("File Content");
        editArea.getStyleClass().add("textEditor");
        editArea.setWrapText(true);
        editArea.setPrefHeight(300.0);
        
        HBox spacer = new HBox();

        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("cancelButton");
        
        Button submit = new Button("Save");
        
        // cancel button action
        cancel.setOnAction(e -> {
            stage.close();
        });
        
        // submit button action
        submit.setOnAction(e -> {
            String newFileName = fileName.getText();
            String fileContent = editArea.getText();
            
            File file = new File(newFileName + ".txt");
            FileHandler.writeFile(file, fileContent);
            StoredFile sFile = StoredFile.store(file, currentDir.getId());
            
            FileHandler.deleteFile(newFileName + ".txt");
            this.add(sFile);
            stage.close();
        });
        
        ColumnConstraints col1 = new ColumnConstraints(75, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        ColumnConstraints col2 = new ColumnConstraints(300, USE_COMPUTED_SIZE, Double.MAX_VALUE);
        ColumnConstraints col3 = new ColumnConstraints(75, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        
        grid.getColumnConstraints().addAll(col1, col2, col3);

        grid.add(fileNameLabel, 0, 0, 3, 1);
        grid.add(fileName, 0, 1, 3, 1);
        
        grid.add(fileContentLabel, 0, 2, 3, 1);
        grid.add(editArea, 0, 3, 3, 1);

        grid.add(submit, 0, 4);
        grid.add(spacer, 1, 4);
        grid.add(cancel, 2,4);

        Scene scene = new Scene(grid, 500, 500, Color.DARKGRAY);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();      
    }
    
    public void remove(FileIcon fileIcon) {        
        this.getChildren().remove(fileIcon);
        if (!fileIcon.getFileObject().isFile()) {
            try {
                userDashboard.removeFromTree((Directory) fileIcon.getFileObject());  
            }
            catch(ClassCastException e) {
                ErrorLogger.logError("Error removing file", e.toString(), true);
            }
        }
    }
}
