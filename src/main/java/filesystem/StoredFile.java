/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filesystem;

import database.DeleteScheduleTable;
import database.DirTable;
import database.FileTable;
import java.io.File;
import java.io.IOException;
import javafx.scene.control.Alert;
import security.EncryptDecryptException;
import utils.ErrorLogger;
/**
 *
 * @author michaels
 */
public class StoredFile implements FileSystemObject, RetrievableFile {

    private static final int STORAGE_NODES = 4;
    private static String file_storage;
    private int id;
    private int parentDir;
    private int ownerId;
    private String name;
    private boolean readOnly;
    
    
    public StoredFile(int id, String name, int parentDir) {
        this.id = id;
        this.name = name;
        this.parentDir = parentDir;
    }
    
    public StoredFile(int id, String name, int parentDir, int ownerId, boolean readOnly) {
        this.ownerId = ownerId;
        this.id = id;
        this.name = name;
        this.parentDir = parentDir;
        this.readOnly = readOnly;
    }
    
    public static StoredFile store(File file, int parentDir) {
        return store(file.getName(), file, parentDir);
    }
    
    /**
     * Store the given file securely in separate directories. <br>
     * @param file
     * @return 
     */
    public static StoredFile store(String fileName, File file, int parentDir) {
        return SecureStorage.store(fileName, file, parentDir);
    }
    
    /**
     * Get the stored file out of storage and write it to the given filepath
     * @param path
     * @return File object
     */
    public File retrieve(String path) throws EncryptDecryptException {
        File file = SecureStorage.retrieveFile(path, id);
        
        if (readOnly) {
            file.setReadOnly();
        }
        
        return file;
    }
    
  
    /**
     * Returns a StoredFile object for given id. <br>
     * Does not return the files stored data - use retrieve to access the data stored to file.
     * @param id
     * @return StoredFile
     */
    public static StoredFile load(int id){
        FileTable fileTable = FileTable.getInstance();
        return fileTable.loadFile(id);
    }
     /**
      * Delete file chunks that are in storage and delete rows in the database that relate to them and this stored file.
      */
    public void delete() {
        SecureStorage.delete(id);
    }
    
    @Override
    public void copy(Directory to) {
        try {
            File tempCopy = File.createTempFile("TEMP_COPY", "");
            tempCopy = this.retrieve(tempCopy.getPath());
            StoredFile.store(this.name,tempCopy, to.getId());
            tempCopy.delete();
        } catch(IOException | EncryptDecryptException e) {
            ErrorLogger.logError("Error copying stored file",e.toString(), true);
        }
    }

    @Override
    public void move(Directory to) {
        updateDir(to.getId());
    }
    
    /**
     * Get the id that belongs to this instance of storedFile
     * @return 
     */
    public int getId() {
        return id;
    }
    
    public int getParentID() {
        return parentDir;
    }
    
    /**
     * Get the name that belongs to this instance of storedFile
     * @return 
     */
    public String getName() {
        return name;
    }
    
    public String getExtension() {
        String extension = "";
        int extensionIndex = name.lastIndexOf('.');
        if (extensionIndex > 0) {
          extension = name.substring(extensionIndex);
        }
        
        return extension;
    }
    
    /**
     * Update the name of this StoredFile instance and persist it into the database.
     * @param name 
     */
    public void updateName(String name) {
        FileTable fileTable = FileTable.getInstance();
        fileTable.updateName(this.id, name);
        this.name = name;
    }
    
    public void updateDir(int dir) {
        FileTable fileTable = FileTable.getInstance();
        fileTable.updateDirectory(this.id, dir);
        this.parentDir = dir;
    }
    
    
    /**
     * @brief Move this file into the Recycling directory. Also creates a delete schedule for this file object.
     */
    public void moveToRecycling() {
        // try to insert a delete schedule for this item and if successful move to the rubbish bin directory
        if (DeleteScheduleTable.getInstance().insert("FILE", id, parentDir)) {
            FileTable.getInstance().updateDirectory(id, -2);
        }
    }
    
    /**
     * @brief Move the file object out of Recycling and back into the directory it was deleted from.
     */
    public void restore() {
        DeleteScheduleTable deleteScheduleTable = DeleteScheduleTable.getInstance();
        int restoreLocation = deleteScheduleTable.loadRestoreLocation("FILE", id);
        if (!DirTable.getInstance().exists(id)) {
            restoreLocation = 0;
        }
        FileTable.getInstance().updateDirectory(id, restoreLocation);
        deleteScheduleTable.delete("FILE", id);
    }
    
    public boolean isFile() {
        return true;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
}
