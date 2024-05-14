/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dashboard;

import customContextMenu.FileContextMenu;
import filesystem.Directory;
import filesystem.FileSystemObject;
import filesystem.SharedFile;
import filesystem.StoredFile;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * 
 * @author michael
 */
public class FileIcon extends VBox {
    Directory currentDir;
    FileSystemObject fileObject;
    Label fileLabel;
    ImageView fileImage;
    ContextMenu contextMenu;
    
    /**
     * Constructor for FileIcon. Takes a StoredFile object as a parameter. <br>
     * Provides context menu for files
     * Extends VBox container and consists of image and label
     * @param fileObject 
     */
    FileIcon(StoredFile fileObject, Directory currentDir) { 
        this.currentDir = currentDir;
        this.fileObject = fileObject;  
        Image image = new Image("file:target/classes/images/fileIcon.png", 50, 0, true, true);
        fileImage = new ImageView(image);
        initialise();
    }
    
    /**
     * Constructor for FileIcon. Takes a StoredFile object as a parameter. <br>
     * Provides context menu for files
     * Extends VBox container and consists of image and label
     * @param fileObject 
     */
    FileIcon(SharedFile fileObject, Directory currentDir) { 
        this.currentDir = currentDir;
        this.fileObject = fileObject;  
        Image image = new Image("file:target/classes/images/fileIcon.png", 50, 0, true, true);
        fileImage = new ImageView(image);
        initialise();
    }

    /**
     * Constructor for directory
     * @param fileObject 
     */
    FileIcon(Directory fileObject, Directory currentDir) {
        this.currentDir = currentDir;
        this.fileObject = fileObject;  
        Image image = new Image("file:src/main/resources/images/folderIcon.png", 50, 0, true, true);
        fileImage = new ImageView(image);
        initialise();
        // double click on folder
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (me.getButton().equals(MouseButton.PRIMARY) && me.getClickCount() == 2) {
                    changeDir();
                }
            }
        }); 
    }
    
    /**
     * Called by the constructor for generic initialisation of object
     */
    private void initialise() {
        contextMenu = new FileContextMenu(this);
        this.setPrefWidth(80);
        this.setPrefHeight(100);
        this.setPadding(new Insets(5));        
        fileLabel = new Label(fileObject.getName());
        fileLabel.setWrapText(true);
        
        StackPane iconImage = getIconImage();
        this.getChildren().addAll(iconImage,fileLabel); 
        
        // add the context menu to the image and label to work with right click
        fileLabel.setContextMenu(contextMenu);
        iconImage.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>(){ 
            public void handle(ContextMenuEvent e) {
               contextMenu.show(iconImage, e.getScreenX(), e.getScreenY());
               e.consume();
            }
        });
    } 
    
    /**
     * Change the directory of the FileViewer that this icon is in
     */
    private void changeDir() {
        Directory dir = (Directory) this.fileObject;
        FileViewer fv = (FileViewer) this.getParent();
        fv.changeDirs(dir);
    }
    
    public FileSystemObject getFileObject() {
        return fileObject;
    }
    
    public Label getLabel() {
        return fileLabel;
    }
    
    public Directory getCurrentDir() {
        return currentDir;
    }
    
    
    /**
     * @brief Return an icon that represents this file object. 
     * @return StackedPane
     */
    private StackPane getIconImage() {
        StackPane fileImages = new StackPane();
        
        if (fileObject.isFile()){
            String fileName = fileObject.getName();
            int extensionIndex = fileName.lastIndexOf(".");
            String extension = fileName.substring(extensionIndex +1);
            String imageFilePath = getFileIconPath(extension);
                        
            ImageView fileImage = new ImageView( new Image(imageFilePath, 50, 0, true, true) );
            fileImages.getChildren().add(fileImage);
            if (fileObject.getClass().getSimpleName().equals("SharedFile")) {
                ImageView shareImage = new ImageView( new Image("file:target/classes/images/share.png", 25, 0, true, true) );
                fileImages.getChildren().add(shareImage);
                StackPane.setAlignment(shareImage, Pos.TOP_RIGHT);
            }
            
            if (fileObject.isReadOnly()){
                ImageView lockImage = new ImageView( new Image("file:target/classes/images/padlock.png", 25, 0, true, true) ); 
                fileImages.getChildren().add(lockImage);
                StackPane.setAlignment(lockImage, Pos.BOTTOM_LEFT);
            }
                        
        }
        else {
            ImageView folderImage = new ImageView( new Image("file:target/classes/images/folderIcon.png", 50, 0, true, true) );
            fileImages.getChildren().add(folderImage);
        }
        
        return fileImages;
    }
    
    
    /**
     * @brief Returns the location of a file icon image. For some extensions an appropriate icon is returned <br>
     * but if one cannot be found then a standard file icon will be chosen.
     * @param extension
     * @return filepath as string
     */
    private static String getFileIconPath(String extension) {
        String imagePath = "file:target/classes/images/fileIcon.png";
        
        String ext = extension.toLowerCase();
        
        switch (ext) {
            case "txt":
                imagePath = "file:target/classes/images/txt.png";
                break;
            case "png":
                imagePath = "file:target/classes/images/png-file.png";
                break;
            case "jpg":
                imagePath = "file:target/classes/images/jpg-file.png";
                break;
            case "zip":
                imagePath = "file:target/classes/images/zip.png";
                break;
        }
        
        return imagePath;
    }
    
}
