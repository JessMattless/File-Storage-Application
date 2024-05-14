/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package customContextMenu;

import dashboard.FileIcon;
import filesystem.LocalClipboard;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

/**
 *
 * @author michael
 */
public class Copy_MenuItem extends MenuItem {
    public Copy_MenuItem(FileIcon fileIcon) {
        super("Copy");
        this.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event) {
                LocalClipboard.copy(fileIcon.getFileObject());
            }
        });
    }
}
