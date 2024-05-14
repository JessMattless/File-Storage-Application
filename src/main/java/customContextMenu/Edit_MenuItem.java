/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package customContextMenu;

import dashboard.FileIcon;
import filesystem.FileSystemObject;
import filesystem.SecureStorage;
import filesystem.StoredFile;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import security.EncryptDecryptException;
import utils.ErrorLogger;
import utils.FileHandler;

/**
 *
 * @author jakem
 */
public class Edit_MenuItem extends MenuItem {
    public Edit_MenuItem(FileIcon fileIcon) {
        super("Edit");
        FileSystemObject fileObject = fileIcon.getFileObject();
        
        this.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event) {
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Edit File");

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                grid.setVgap(30);
                grid.setPadding(new Insets(20, 20, 20, 20));

                TextArea editArea = new TextArea();
                editArea.getStyleClass().add("textEditor");
                editArea.setWrapText(true);
                editArea.setPrefHeight(300.0);
                
                StoredFile storedFile = new StoredFile(fileObject.getId(), fileObject.getName(), fileObject.getParentID());
                try {
                    File f = storedFile.retrieve("/tmp/" + fileObject.getName());
                    List<String> fileText = FileHandler.readFile(f);
                    for (String line : fileText) {
                        editArea.appendText(line + "\n");
                    }
                    FileHandler.deleteFile("/tmp/" + fileObject.getName());
                }
                catch (EncryptDecryptException e) {
                    ErrorLogger.logError("Error when editing file", e.toString(), true);
                }

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
                    File f = new File("/tmp/" + fileObject.getName());
                    FileHandler.writeFile(f, editArea.getText());
                    
//                    SecureStorage.store(fileObject.getName(), f, fileObject.getParentID());
// Instead of editing existing file, when creating a new file, allow you to add the contents within the same window.
                    
                    FileHandler.deleteFile("/tmp/" + fileObject.getName());
                    
                    stage.close();
                });

                ColumnConstraints col1 = new ColumnConstraints(75, -1.0, -1.0);
                ColumnConstraints col2 = new ColumnConstraints(300, -1.0, Double.MAX_VALUE);
                ColumnConstraints col3 = new ColumnConstraints(75, -1.0, -1.0);

                grid.getColumnConstraints().addAll(col1, col2, col3);

//                grid.add(fileNameLabel, 0, 0, 3, 1);
//                grid.add(fileName, 0, 1, 3, 1);

                grid.add(editArea, 0, 0, 3, 1);

                grid.add(submit, 0, 1);
                grid.add(spacer, 1, 1);
                grid.add(cancel, 2,1);

                Scene scene = new Scene(grid, 500, 400, Color.DARKGRAY);
                scene.getStylesheets().add("style.css");
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            }
        });
    }
}
