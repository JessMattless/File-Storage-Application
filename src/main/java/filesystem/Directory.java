/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filesystem;

import com.mycompany.group_9_comp20081_app.App;
import database.DeleteScheduleTable;
import database.DirTable;
import database.FileTable;
import java.util.ArrayList;

/**
 *
 * @author michael
 */
public class Directory implements FileSystemObject {
    private int id;
    private Directory parentDir;
    private String name;
    private static final String ROOT_DIR = "Home";
    private static final String SHARED_DIR = "Shared";
    private static final String TRASH_DIR = "Recycle";
    
    public Directory(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Directory(int id, String name, Directory parentDir) {
        this.id = id;
        this.name = name;
        this.parentDir = parentDir;
    }
    
    /**
     * If no parent directory is given to the create function then default is root directory
     * @param name
     * @return Directory object
     */
    public static Directory create(String name) {
        return create(name, new Directory(0, ROOT_DIR));
    }
    
    /**
     * Insert a row into directory table and return a Directory object
     * @param name
     * @param parentDir
     * @return Directory
     */
    public static Directory create(String name, Directory parentDir) {
        DirTable dirTable = DirTable.getInstance();
        return dirTable.insert(App.getCurrentUser().getId(), name, parentDir);
    }
    
    /**
     * Will delete all subdirectories and files that belong to this directory as well as this directory
     */
    @Override
    public void delete() {
        ArrayList<FileSystemObject> fileObjects = new ArrayList<>();
        int userId = App.getCurrentUser().getId();
        fileObjects.addAll(DirTable.getInstance().getUserDirs(userId, this));
        fileObjects.addAll(FileTable.getInstance().getUserFiles(userId, id));
        for (FileSystemObject fso: fileObjects) {
            fso.delete();
        }
        DirTable.getInstance().delete(id);
        // remove row delete schedule table that relates to this directory
        DeleteScheduleTable.getInstance().delete("DIR", id);
    }

    @Override
    public void copy(Directory to) {
        Directory copiedDir = Directory.create(name, to);
        
        ArrayList<FileSystemObject> fileObjects = new ArrayList<>();
        int userId = App.getCurrentUser().getId();
        fileObjects.addAll(DirTable.getInstance().getUserDirs(userId, this));
        fileObjects.addAll(FileTable.getInstance().getUserFiles(userId, id));
        for (FileSystemObject fso: fileObjects) {
            fso.copy(copiedDir);
        }

    }
    
    public String getExtension() {
        // Return an empty string, since a folder has no extension
        return "";
    }

    public String toString() {
        return this.name;
    }

    @Override
    public void move(Directory to) {
        updateDir(to.getId());
    }

    @Override
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Directory getParent() {
        return parentDir;
    }
    
    public int getParentID() {
        return parentDir.getId();
    }

    public void setParentDir(Directory parentDir) {
        this.parentDir = parentDir;
    }
    
    @Override
    public void updateName(String name) {
        DirTable.getInstance().updateName(id, name);
    }
    
    public void updateDir(int dir) {
        DirTable.getInstance().updateDirectory(id, dir);
    }
    
    /**
     * Return an instance of Directory that represents the root directory
     * @return Directory root
     */
    public static Directory root() {
        return new Directory(0, ROOT_DIR);
    }
    
    /**
     * Return an instance of Directory that represents the shared directory
     * @return Directory shared
     */
    public static Directory shared() {
        return new Directory(-1, SHARED_DIR);
    }
    
    /**
     * Return an instance of Directory that represents the recycling bin directory
     * @return Directory recycling bin
     */
    public static Directory recycle() {
        return new Directory(-2, TRASH_DIR);
    }
    
    /**
     * Get a string representation of this directory filepath. <br>
     * This is not a real filepath but is useful for UX
     * @return String filepath
     */
    public String getPath() {
        StringBuilder path = new StringBuilder();
        path.append(name);
        
        Directory parentDir = getParent();
        
        while (parentDir != null) {
            path.insert(0, (parentDir.getName() + "/"));
            parentDir = parentDir.getParent(); 
        }
        
        return path.toString();
    }
    
    /**
     * Return the root directory this directory belongs to
     * @return Directory
     */
    public Directory getRoot() {
        Directory root = this;
        
        while (root.getParent() != null) {
            root = root.getParent();
        }   
        return root;
    }
    
    /**
     * Move this directory to the recycling bin directory
     */
    public void moveToRecycling() {
        if (DeleteScheduleTable.getInstance().insert("DIR", id, parentDir.getId())) {
            DirTable.getInstance().updateDirectory(id, -2);
        }
    }
    
    /**
     * @brief Restores the deleted Directory and all its contents. Will attempt to restore to original location<br>
     * but failing that will restore to the home directory.
     */
    public void restore() {
        DeleteScheduleTable deleteScheduleTable = DeleteScheduleTable.getInstance();
        int restoreLocation = deleteScheduleTable.loadRestoreLocation("DIR", id);
        if (!DirTable.getInstance().exists(id)) {
            restoreLocation = 0;
        }
        DirTable.getInstance().updateDirectory(id, restoreLocation);
        deleteScheduleTable.delete("DIR", id);
    }
    
    /**
     * @brief Used to get the type of file object when using the abstract FileSystemObject class
     * @return false
     */
    public boolean isFile() {
        return false;
    }
    
    public boolean isReadOnly() {
        return false;
    }

}
