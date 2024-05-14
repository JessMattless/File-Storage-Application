/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package customContextMenu;

import dashboard.FileIcon;
import filesystem.Directory;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import utils.ErrorLogger;

/**
 *
 * @author michael
 */
public class FileContextMenu extends ContextMenu {
    public FileContextMenu(FileIcon fileIcon) {
        Directory currentDir = fileIcon.getCurrentDir();
        switch (currentDir.getRoot().getId()) {
            case 0:
                // If it is a text file, allow editing and viewing file options
                if (fileIcon.getFileObject().getExtension().equals(".txt")) {
                    this.getItems().addAll(
//                            new Edit_MenuItem(fileIcon),
                        new View_MenuItem(fileIcon)
                    );
                }
                this.getItems().addAll(
                    new Cut_MenuItem(fileIcon),
                    new Copy_MenuItem(fileIcon),
                    new Rename_MenuItem(fileIcon),
                    new Delete_MenuItem(fileIcon)
                );
                if (fileIcon.getFileObject().isFile()) {
                    this.getItems().addAll(
                        new Share_MenuItem(fileIcon),
                        new Download_MenuItem(fileIcon)
                    );
                }
                break;
                
            case -1:
                // If it is a text file, allow editing and viewing file options
                if (fileIcon.getFileObject().getExtension().equals(".txt")) {
                    this.getItems().addAll(
//                            new Edit_MenuItem(fileIcon),
                        new View_MenuItem(fileIcon)
                    );
                }
                this.getItems().addAll(
                    new Cut_MenuItem(fileIcon),
                    new Copy_MenuItem(fileIcon),
                    new Delete_MenuItem(fileIcon)
                );
                
                this.getItems().add(new Download_MenuItem(fileIcon));
                break;
                
            case -2:
                if (currentDir.getId() == -2) {
                    this.getItems().addAll(
                        new Restore_MenuItem(fileIcon),
                        new PermanentDel_MenuItem(fileIcon)
                    );
                }
                else {
                    this.getItems().addAll(
                        new Cut_MenuItem(fileIcon),
                        new Copy_MenuItem(fileIcon),
                        new PermanentDel_MenuItem(fileIcon)
                    );
                }
                break;
        }
    }
}
