/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package customContextMenu;

import dashboard.FileViewer;
import filesystem.LocalClipboard;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

/**
 *
 * @author michael
 */
public class Paste_MenuItem extends MenuItem {
    public Paste_MenuItem(FileViewer fileViewer) {
        super("Paste");
        this.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event) {
                LocalClipboard.paste(fileViewer.getCurrentDir());
                fileViewer.refreshList();
            }
        });
    }
}
