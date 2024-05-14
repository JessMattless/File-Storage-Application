/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package customContextMenu;

import dashboard.FileViewer;
import filesystem.LocalClipboard;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.WindowEvent;

/**
 *
 * @author michael
 */
public class FileViewContextMenu extends ContextMenu {
    FileViewer fileViewer;
    
    public FileViewContextMenu(FileViewer fileViewer) {
        this.fileViewer = fileViewer;
        setItems();
    }
    
    public void refresh() {
        clear();
        setItems();
    }
    
    private void clear() {
        this.getItems().clear();
    }
    
    private void setItems() {
        if (fileViewer.getCurrentDir().getRoot().getId() == 0) {
            MenuItem pasteMenuItem = new Paste_MenuItem(fileViewer);
            this.getItems().addAll(
                new CreateDir_MenuItem(fileViewer),
                pasteMenuItem
            );
            this.setOnShowing(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    pasteMenuItem.setDisable(!LocalClipboard.hasFile());
                }
            });
        }
    }
}
