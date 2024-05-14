/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import filesystem.Directory;
import static database.DataBase.timeout;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import utils.ErrorLogger;

/**
 * @author michael
 */
public class DirTable extends DataBase {
    
    private static DirTable instance = null;
    
    private DirTable(){};
    
    public static DirTable getInstance() {
        if (instance == null) {
            instance = new DirTable();
        }
        return instance;
    }
    
    
    public Directory insert(int user, String name, Directory dir) {
        Directory directory = null;
        
        // insert user to table
        String ddl = "INSERT INTO Directory (owner, name, directoryID) VALUES ( ?, ?, ? );";
            
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl, PreparedStatement.RETURN_GENERATED_KEYS);
                                  
            preparedStatement.setInt(1, user);
            preparedStatement.setString(2, name);
            preparedStatement.setInt(3, dir.getId());
           
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
            
            // get the auto generated keys from insert 
            ResultSet keys = preparedStatement.getGeneratedKeys();
            
            // get the id value 
            int key = keys.getInt(1);
            directory = new Directory(key, name, dir);
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error inserting directory row",e.toString(), true);
        } finally {
            disconnect();
        }
        return directory;
    }
    
    /**
     * Update the parent directory id of the directory
     * @param id
     * @param dir 
     */
    public void updateDirectory(int id, int dir) {
        
        String ddl = "UPDATE Directory SET directoryID = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                        
            preparedStatement.setInt(1, dir);
            preparedStatement.setInt(2, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating directory table row",e.toString(), false);
        } finally {
            disconnect();
        }
    }
    
    /**
     * Update the name of the directory
     * @param id
     * @param name 
     */
    public void updateName(int id, String name) {
        
        String ddl = "UPDATE Directory SET name = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                        
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating name in directory row",e.toString(), true);
        } finally {
            disconnect();
        }
    }
    
    public void delete(int id) {
        
        String dml = "DELETE FROM Directory WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(dml);
            
            preparedStatement.setInt(1, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error deleting directory row",e.toString(), true);
        } finally {
            disconnect();
        }
    }
    
    /**
     * This method is called if no parent directory is given as argument. <br>
     * Will create a root directory instance and call overloaded method
     * @param userId
     * @return ArrayList of StoredFile objects
     */
    public ArrayList<Directory> getUserDirs(int userId) {
        return getUserDirs(userId, Directory.root());
    }
    
    /**
     * Return an ArrayList of directories that are in the given directory
     * @param userId
     * @param dir
     * @return ArrayList of StoredFile objects
     */
    public ArrayList<Directory> getUserDirs(int userId, Directory parentDir) {
        
        ArrayList<Directory> dirs = new ArrayList<>();
        
        try {
            connect();
            
            String query = "SELECT * FROM Directory WHERE owner = ? AND directoryID = ? ORDER BY name;";
            
            var statement = connection.prepareStatement(query);
            
            statement.setInt(1, userId);
            statement.setInt(2, parentDir.getId());
            
            statement.setQueryTimeout(timeout);
            
            ResultSet results = statement.executeQuery();      
            
            while (results.next()) {
                int dirId = results.getInt("id");
                String name = results.getString("name");
                
                Directory dir = new Directory(dirId, name, parentDir);
                dirs.add(dir);
            }
        } catch (SQLException e) {
            ErrorLogger.logError("Error getting user directories",e.toString(), true);
        } finally {
            disconnect();
        }            
        return dirs;
    }
    
    
    public boolean exists(int id) {
        boolean exists = true;

        String query = "SELECT COUNT(*) FROM Directory WHERE id = ?;";
        
        try {
            connect();
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.setInt(1, id);
            ResultSet results = preparedStatement.executeQuery();
            
            exists = results.next();
        } catch (SQLException e) {
            ErrorLogger.logError("Error when checking directory exists in table",e.toString(), false);
        } finally {
            disconnect();
        }
        
        return exists;
    }
}
