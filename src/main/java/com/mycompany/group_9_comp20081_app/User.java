/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.group_9_comp20081_app;

import utils.Helper;
import filesystem.StoredFile;
import filesystem.Directory;
import database.FileTable;
import database.DirTable;
import database.SharedFileTable;
import database.UserTable;
import filesystem.SharedFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import security.EncryptDecryptException;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import security.RSA;
import utils.ErrorLogger;
import utils.FileHandler;

/**
 *
 * @author michael
 */
public class User {
    private int id;
    private String name;
    private String lastLogin;
    private String password;
    private int admin;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public User(int id , String name, String lastLogin, int admin) {
        this.id = id;
        this.name = name;
        this.lastLogin = lastLogin;
        this.admin = admin;
    }
    
    public User(int id , String name, String lastLogin, String password, int admin) {
        this.id = id;
        this.name = name;
        this.lastLogin = lastLogin;
        this.password = password;
        this.admin = admin;
    }
    
    /**
     * Constructor for user when loading from DB
     * @param id
     * @param name
     */
    public User(int id , String name) {
        this.id = id;
        this.name = name;
    }
    
    /**
     * Loads user data from the user table for given username and password 
     * and instantiates a user object with this data. Sets the last login
     * attribute.
     * @param name
     * @param password
     * @return 
     */
    public static User login(String name, String password){
        UserTable userTable = UserTable.getInstance();
        User user = userTable.loadUser(name, password);
        
        if (user != null) {
            String timestamp = Helper.getTimestamp();
            user.setLastLogin(timestamp);
            userTable.updateLastLogin(user.getId(), timestamp);
            
            user.successfulLogin();
          
        }
        return user;
    }
    
        /**
     * Insert a row into the user table with given username and password.
     * Returns a user object.
     * @param name
     * @param password
     * @return User object
     */
    public static User create(String name, String password) {
        UserTable userTable = UserTable.getInstance();
        User user = userTable.insert(name, password);
        if (user != null) {
            user.successfulLogin();
        }
        return user;
    }
    
    /**
     * This method is called when a User has either successfully logged in or registered. It retrieves the users RSA keys 
     * required for encrypting and decrypting files.
     */
    public void successfulLogin() {
        try {
            File privateKeyFile = new File(".user_" + id +  "_private.key");
            byte[] publicKeyBytes;
            if (!privateKeyFile.exists()) {
                KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
                keyGenerator.initialize(2048);
                KeyPair keyPair = keyGenerator.generateKeyPair();
                PrivateKey privateKey = keyPair.getPrivate();
                PublicKey publicKey = keyPair.getPublic();
                
                publicKeyBytes = publicKey.getEncoded();
                
                UserTable.getInstance().updatePublicKey(id, publicKeyBytes);
                
                try (FileOutputStream fos = new FileOutputStream(privateKeyFile)) {
                    fos.write(privateKey.getEncoded());
                    fos.close();
                }
                
            }
            else {
                publicKeyBytes = UserTable.getInstance().loadPublicKey(id);
            }
            
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
           
            publicKey = RSA.publicKeyFromBytes(publicKeyBytes);
            privateKey = RSA.privateKeyFromBytes(privateKeyBytes);
                                                
        }
        catch (IOException | NoSuchAlgorithmException | EncryptDecryptException  e) {
            ErrorLogger.logError("Error logging in", e.toString(), false);
//            Platform.runLater(() -> alert.show());
        }
    }
    
    /**
     * Attempt to log in the user for given id
     * @param userID
     * @return User object
     */
    public static User login(int userID){
        UserTable userTable = UserTable.getInstance();
        User user = userTable.loadUser(userID);
        
        if (user != null) {
            String timestamp = Helper.getTimestamp();
            user.setLastLogin(timestamp);
            userTable.updateLastLogin(user.getId(), timestamp);
            
            user.successfulLogin();
        }
        return user;
    }
    
    /**
     * Loads user data from the user table for given username and password 
     * and instantiates a user object with this data.
     * @param name
     * @param password
     * @return User object
     */
    public static User load(String name, String password){
        UserTable userTable = UserTable.getInstance();
        User user = userTable.loadUser(name, password);
        if (user != null) {
            user.successfulLogin();
        }
        return user;
    }
    
    /**
     * Loads user data from the user table for given user ID
     * and instantiates a user object with this data.
     * @param userID
     * @return
     */
    public static User load(int userID) {
        UserTable userTable = UserTable.getInstance();
        User user = userTable.loadUser(userID);
        if (user != null) {
            user.successfulLogin();
        }
        return user;
    }
    
    public static boolean doesTemporaryFileExist(int userID) {
        File tmpFolder = new File("/tmp/");
        boolean exists = false;
        
        if(tmpFolder.exists() && tmpFolder.isDirectory()) {
            String[] fileList = tmpFolder.list();
            for (String fileName : fileList) {
                if (fileName.contains("FileStash-" + userID)) {
                    exists = true;
                    break;
                }
            }
        }
        
        return exists;
    }
    
    public void generateTemporaryFile() {
        try {
            File tmpLoginFile = File.createTempFile("FileStash-" + this.id + "-", ".tmp");
            tmpLoginFile.deleteOnExit();
        }
        catch (IOException e) {
            ErrorLogger.logError("Error generating temporary file", e.toString(), false);
        }
    }
    
    public void deleteTempFileIfExists() {
        File tmpFolder = new File("/tmp/");
        boolean exists = false;

        if(tmpFolder.exists() && tmpFolder.isDirectory()) {
            String[] fileList = tmpFolder.list();
            for (String fileName : fileList) {
                if (fileName.contains("FileStash-" + this.id)) {
                    FileHandler.deleteFile("/tmp/" + fileName);
                    break;
                }
            }
        }
    }
    
    /**
     * Returns whether a user exists given a username/password.
     * @param username
     * @param password
     * @return
     */
    public static boolean exists(String username, String password) {
        UserTable userTable = UserTable.getInstance();
        User user = userTable.loadUser(username, password);
        if (user != null) {
            return true;
        }
        return false;
    }
    
    public void generateLoginFile() {
        FileHandler.deleteFile("login.dat");
        FileHandler.createFile("login.dat");
        FileHandler.writeFile("login.dat", Integer.toString(this.id));
    }
    
    public void deleteLoginFile() {
        FileHandler.deleteFile("login.dat");
    }
    
    public static User getUserFromLoginFile() {
        UserTable userTable = UserTable.getInstance();
        User user = null;
        int userID;
        List<String> loginData = FileHandler.readFile("login.dat");
        
        if (!loginData.isEmpty()) {
            userID = Integer.parseInt(loginData.get(0));
            
            user = User.login(userID);
        }
        
        return user;
    }
    
    /**
     * Persist the given password string into the database for this user.
     * @param password 
     */
    public void updatePassword(String password) {
        UserTable userTable = UserTable.getInstance();
        userTable.updatePassword(id, password);
    }
    
    /**
     * Persist the given username string into the database for this user.
     * @param name 
     */
    public void updateName(String name) {
        UserTable userTable = UserTable.getInstance();
        this.name = name;
        userTable.updateName(id, name);
    }
    
    /**
     * Gets a list of all the stored files that are related to this user
     * @return 
     */
    public ArrayList<StoredFile> getFiles() {
        FileTable fileTable = FileTable.getInstance();
        return fileTable.getUserFiles(this.id);
    }
    
    /**
     * Gets a list of all the stored files that are related to this user
     * @return 
     */
    public ArrayList<StoredFile> getFiles(int parentDirID) {
        FileTable fileTable = FileTable.getInstance();
        return fileTable.getUserFiles(this.id, parentDirID);
    }
    
    /**
     * Gets a list of all the stored files that are related to this user
     * @return 
     */
    public ArrayList<Directory> getDirs() {
        DirTable dirTable = DirTable.getInstance();
        return dirTable.getUserDirs(this.id);
    }
    
    /**
     * Gets a list of all the stored files that are related to this user
     * @return 
     */
    public ArrayList<Directory> getDirs(Directory parent) {
        DirTable dirTable = DirTable.getInstance();
        return dirTable.getUserDirs(this.id, parent);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    public int getPrivileges() {
        return admin;
    }
        
    /**
     * Gets a list of all the shared files that are related to this user
     * @return 
     */
    public ArrayList<SharedFile> getSharedFiles(int parentDirId) {
        return SharedFileTable.getInstance().getUserSharedFiles(id, parentDirId);
    }
    
    
    /**
     * Delete this users row from the database
     */
    public void delete() {
        UserTable userTable = UserTable.getInstance();
        userTable.delete(this);
    }
    
    /**
     * Get this user id
     * @return user id
     */
    public int getId() {
        return id;
    }

    /**
     * Set this user id. Does not persist.
     * @param id 
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get this username
     * @return username for this user
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the users name. Does not persist.
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the hashed password for this user from the database.
     * @return 
     */
    public String retrievePassword() {
        UserTable userTable = UserTable.getInstance();
        String password = userTable.loadPassword(this);
        return password;
    }
    
    /**
     * Get the last login stored in this instance. 
     * @return String last login timestamp
     */
    public String getLastLogin() {
        return lastLogin;
    }
    
    /**
     * Set the last login. Does not persist.
     * @param lastLogin 
     */
    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }
}
