/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package customContextMenu;

import dashboard.FileIcon;
import filesystem.FileSystemObject;
import filesystem.RetrievableFile;
import java.io.File;
import java.nio.file.Path;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import security.EncryptDecryptException;
import utils.ErrorLogger;

/**
 *
 * @author michael
 */
public class Download_MenuItem extends MenuItem {
    
    public Download_MenuItem(FileIcon fileIcon) throws IllegalArgumentException {
        super("Download");
        
        FileSystemObject fileObject = fileIcon.getFileObject();
        
        try {
            RetrievableFile retrievableFile = (RetrievableFile) fileObject;
            
            this.setOnAction(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent event) {
                    Stage stage = new Stage();
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    File downloadsFolder = new File(System.getProperty("user.home") + "/Downloads/");
                    if (downloadsFolder.exists()) {
                        directoryChooser.setInitialDirectory(downloadsFolder);
                    }
                    File selectedDir= directoryChooser.showDialog(stage);
                    if (selectedDir != null) {
                        Path path = Path.of(selectedDir.toString(), fileObject.getName());
                        try {
                            retrievableFile.retrieve(path.toString());
                        }
                        catch (EncryptDecryptException e) {
                            ErrorLogger.logError("Error when downloading file", e.toString(), true);
                        }

                    }              
                }
            });
        }
        catch (ClassCastException e) {
            ErrorLogger.logError("Error when downloading file", e.toString(), true);
        }
    }
}
