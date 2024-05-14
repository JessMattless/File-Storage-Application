/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filesystem;

import com.mycompany.group_9_comp20081_app.User;
import database.DeleteScheduleTable;
import database.DirTable;
import database.FileTable;
import database.SharedFileTable;
import java.io.File;
import java.io.IOException;
import javafx.scene.control.Alert;
import security.EncryptDecryptException;
import utils.ErrorLogger;

/**
 *
 * @author michael
 */
public class SharedFile implements FileSystemObject, RetrievableFile {
    private int id;
    private boolean readOnly;
    private StoredFile fileObject;
    private final Directory parentDir = Directory.shared();
    
    public SharedFile(int id, boolean readOnly, StoredFile fileObject) {
        this.id = id;
        this.readOnly = readOnly;
        this.fileObject = fileObject;
        this.fileObject.setReadOnly(readOnly);
    }
    
    public String getExtension() {
        String extension = "";
        int extensionIndex = fileObject.getName().lastIndexOf('.');
        if (extensionIndex > 0) {
          extension = fileObject.getName().substring(extensionIndex);
        }
        
        return extension;
    }
    
    /**
     * @brief Delete the corresponding row in the database. This is a permanent delete of the shared file instance.
     */
    @Override
    public void delete() {
        SharedFileTable.getInstance().delete(id);
    }
    
    /**
     * @brief Load the stored file this shared file is pointing to and store a copy in this users file system.
     * @param to The directory to store the copy to.
     */
    @Override
    public void copy(Directory to) {
        try {
            File tempCopy = File.createTempFile("TEMP_COPY", "");
            User fileOwner = FileTable.getInstance().GetFileOwner(fileObject.getId());
            tempCopy = retrieve(tempCopy.getPath());
            if(readOnly) {
                tempCopy.setReadOnly();
            }
            StoredFile.store(fileObject.getName(),tempCopy, to.getId());
            tempCopy.delete();
        } catch(IOException | EncryptDecryptException e) {
            ErrorLogger.logError("Error copying shared file",e.toString(), true);
        }
    }
    
    /**
     * @brief Load the stored file this shared file is pointing to and store a copy in this users file system<br>
     * then delete this shared file instance.
     * @param to The directory to store the copy to.
     */
    @Override
    public void move(Directory to) {
        copy(to);
        SharedFileTable.getInstance().delete(id);
    }

    @Override
    public int getId() {
        return -2;
    }

    @Override
    public int getParentID() {
        return parentDir.getId();
    }

    @Override
    public String getName() {
        return fileObject.getName();
    }
    
    public StoredFile getStoredFile() {
        return fileObject;
    }

    @Override
    public void updateName(String name) {
        if (!readOnly) {
            fileObject.updateName(name);
        }
    }
    
    public File retrieve(String path) throws EncryptDecryptException  {
        File file = SecureStorage.retrieveSharedFile(path, id, fileObject.getId());
        
        if (readOnly) {
            file.setReadOnly();
        }
        
        return file;
    }
    
    /**
     * @brief Move this shared file into the Recycling directory. Also creates a delete schedule for this file object.
     */
    @Override
    public void moveToRecycling() {
        if (DeleteScheduleTable.getInstance().insert("SHARED_FILE", id, -1)) {
            SharedFileTable.getInstance().updateDirectory(id, -2);
        }
    }
    
    /**
     * @brief Move the file object out of Recycling and back into the directory it was deleted from.
     * In this case it will go back to the shared directory.
     */
    @Override
    public void restore() {
        DeleteScheduleTable deleteScheduleTable = DeleteScheduleTable.getInstance();
        int restoreLocation = deleteScheduleTable.loadRestoreLocation("SHARED_FILE", id);
        if (!DirTable.getInstance().exists(id)) {
            restoreLocation = 0;
        }
        SharedFileTable.getInstance().updateDirectory(id, restoreLocation);
        deleteScheduleTable.delete("SHARED_FILE", id);
    }
    
    /**
     * Shared files can only point to file objects currently so this always returns true.
     * @return true 
     */
    @Override
    public boolean isFile() {
        return true;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
}
