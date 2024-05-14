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
public class Cut_MenuItem extends MenuItem {
    public Cut_MenuItem(FileIcon fileIcon) {
        super("Cut");
        this.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event) {
                LocalClipboard.cut(fileIcon.getFileObject());
            }
        });
    }
}
