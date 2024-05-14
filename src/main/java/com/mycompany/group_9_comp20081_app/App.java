package com.mycompany.group_9_comp20081_app;

import database.DataBase;
import database.UserTable;
import filesystem.FileDeleteMonitor;
import filesystem.SecureStorage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import javafx.scene.image.Image;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static User currentUser;
                
    @Override
    public void start(Stage stage) throws IOException {
        
        DataBase db = new DataBase();
        db.createTables();
        UserTable userTable = UserTable.getInstance();
        if (!userTable.exists("admin")) {
            userTable.insertDefaultAdminUser();
        }
        
        
        Thread backgroundDeleteMonitor = new Thread(new FileDeleteMonitor());
        backgroundDeleteMonitor.setDaemon(true);
        backgroundDeleteMonitor.start();
        
        // to create the directories to simulate storage containers
        Path workingDirectory = Path.of(System.getProperty("user.dir"));
        String file_storage = workingDirectory.getParent().toString() + "/file_storage/";
        SecureStorage.createFileStorage(file_storage);
        
        User user = User.getUserFromLoginFile();
        if (user != null && !User.doesTemporaryFileExist(user.getId())) {
            setCurrentUser(user);
            user.generateTemporaryFile();
            scene = new Scene(loadFXML("dashboard"), 680, 480);
        }
        else {
            user = null; // This is here in case the user is already logged in on another instance
            scene = new Scene(loadFXML("loginScreen"), 680, 480);
        }
        
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("FileStash");
//        stage.getIcons().add(new Image("file:target/classes/images/penguin.png"));
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    public static Scene getScene() {
        return scene;
    }

}