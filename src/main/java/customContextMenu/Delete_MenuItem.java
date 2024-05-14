/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package customContextMenu;

import dashboard.FileIcon;
import dashboard.FileViewer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

/**
 *
 * @author michael
 */
public class Delete_MenuItem extends MenuItem {
    
    public Delete_MenuItem(FileIcon fileIcon) {
        super("Delete");
        this.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event) {
                FileViewer fileViewer = (FileViewer) fileIcon.getParent();
                fileViewer.remove(fileIcon);
                fileIcon.getFileObject().moveToRecycling();
            }
        });
    }
}
