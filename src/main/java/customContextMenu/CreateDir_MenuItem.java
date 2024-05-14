/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package customContextMenu;

import dashboard.FileViewer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

/**
 *
 * @author michael
 */
public class CreateDir_MenuItem extends MenuItem {
    public CreateDir_MenuItem(FileViewer fileViewer) {
        super("Create Directory");
        this.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event) {
                fileViewer.createDirectory();
            }
        });
    }
}
