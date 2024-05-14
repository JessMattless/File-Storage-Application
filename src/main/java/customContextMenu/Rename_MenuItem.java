/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package customContextMenu;

import dashboard.FileIcon;
import filesystem.FileSystemObject;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author michael
 */
public class Rename_MenuItem extends MenuItem {
    public Rename_MenuItem(FileIcon fileIcon) {
        super("Rename");
        FileSystemObject fileObject = fileIcon.getFileObject();
        
        this.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event) {
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Rename File");

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                grid.setVgap(30);
                grid.setPadding(new Insets(20, 20, 20, 20));

                Label fileNameLabel = new Label("Enter New File Name: ");
                fileNameLabel.setStyle("-fx-font: 16 arial;");
                TextField fileName = new TextField();
                fileName.setPromptText("New File Name");
                fileName.setPrefHeight(40);

                HBox spacer = new HBox();

                Button cancel = new Button("Cancel");
                cancel.getStyleClass().add("cancelButton");

                Button submit = new Button("Submit");

                // cancel button action
                cancel.setOnAction(e -> {
                    stage.close();
                });

                // submit button action
                submit.setOnAction(e -> {
                    String extension = fileObject.getExtension();
                    String newFileName = fileName.getText();
                    fileObject.updateName(newFileName + extension);
                    fileIcon.getLabel().setText(newFileName + extension);
                    stage.close();
                });

                ColumnConstraints col1 = new ColumnConstraints(75, -1.0, -1.0);
                ColumnConstraints col2 = new ColumnConstraints(100, -1.0, Double.MAX_VALUE);
                ColumnConstraints col3 = new ColumnConstraints(75, -1.0, -1.0);

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
        });
    }
}
