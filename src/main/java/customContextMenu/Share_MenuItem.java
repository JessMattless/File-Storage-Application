/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package customContextMenu;

import com.mycompany.group_9_comp20081_app.UserSelectModal;
import dashboard.FileIcon;
import filesystem.StoredFile;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import utils.ErrorLogger;

/**
 *
 * @author michael
 */
public class Share_MenuItem extends MenuItem {
    public Share_MenuItem(FileIcon fileIcon) {
        super("Share");
        this.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event) {
                Parent root;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("UserSelectModal.fxml"));
                    root = loader.load();
                    Stage stage = new Stage();
                    stage.setTitle("Share File With User");
                    stage.setScene(new Scene(root, 450, 450));
                    stage.show();
                 
                    UserSelectModal userSelectModal = loader.getController();
                    userSelectModal.linkFile((StoredFile) fileIcon.getFileObject());
                }
                catch (IOException e) {
                    ErrorLogger.logError("Error sharing file", e.toString(), true);
                }
            }
        });
    }
}
