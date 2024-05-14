/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import filesystem.SharedFile;
import static database.DataBase.timeout;
import filesystem.StoredFile;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import utils.ErrorLogger;

/**
 * @author michael
 */
public class SharedFileTable extends DataBase {
    
    private static SharedFileTable instance = null;
    
    private SharedFileTable(){};
    
    public static SharedFileTable getInstance() {
        if (instance == null) {
            instance = new SharedFileTable();
        }
        return instance;
    }
    
    
    /**
     * @brief Inserts a row into the shared file table
     * @param userID
     * @param fileID
     * @param writePrivilege
     * @param aesKey
     * @return SharedFile object that corresponds to the row
     */
    public SharedFile insert(int userID, int fileID, boolean writePrivilege, byte[] aesKey) {
        SharedFile sharedFile = null;
        
        // insert user to table
        String ddl = "INSERT INTO SharedFile (owner, fileID, writePrivilege, aesKey, directoryID) VALUES ( ?, ?, ?, ?, -1 );";
            
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl, PreparedStatement.RETURN_GENERATED_KEYS);
                                  
            preparedStatement.setInt(1, userID);
            preparedStatement.setInt(2, fileID);
            preparedStatement.setBoolean(3, writePrivilege);
            preparedStatement.setBytes(4, aesKey);
           
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
            
            StoredFile file = FileTable.getInstance().loadFile(fileID);
            // get the auto generated keys from insert 
            ResultSet keys = preparedStatement.getGeneratedKeys();
            
            // get the id value 
            int key = keys.getInt(1);
            sharedFile = new SharedFile(key, !writePrivilege, file);
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error inserting shared file table row",e.toString(), true);
        } finally {
            disconnect();
        }
        return sharedFile;
    }

    /**
     * @brief Deletes the row found by the given id
     * @param id 
     */
    public void delete(int id) {
        // update table data with user data
        
        String dml = "DELETE FROM SharedFile WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(dml);
            
            preparedStatement.setInt(1, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error deleting shared file table row",e.toString(), true);
        } finally {
            disconnect();
        }
    }

    /**
     * @brief Gets a list of all SharedFile objects in the requested directory that belong to the user found by id
     * @param userId
     * @param dir
     * @return ArrayList<SharedFile> 
     */
    public ArrayList<SharedFile> getUserSharedFiles(int userId, int dir) {
        
        ArrayList<SharedFile> sharedFiles = new ArrayList<>();
        
        try {
            connect();
            
            String query = "SELECT * FROM SharedFile WHERE owner = ? AND directoryID = ?;";
            
            var statement = connection.prepareStatement(query);
            
            statement.setInt(1, userId);
            statement.setInt(2, dir);
            
            statement.setQueryTimeout(timeout);
            
            ResultSet results = statement.executeQuery();      
            
            while (results.next()) {
                int id = results.getInt("id");
                int fileID = results.getInt("fileID");
                boolean writePrivilege = results.getBoolean("writePrivilege");
                
                StoredFile file = FileTable.getInstance().loadFile(fileID);
                
                SharedFile sharedFile = new SharedFile(id, !writePrivilege, file);
                sharedFiles.add(sharedFile);
            }
        } catch (SQLException e) {
            ErrorLogger.logError("Error getting list of shared files from table",e.toString(), true);
        } finally {
            disconnect();
        }            
        return sharedFiles;
    }
    
    
    /**
     * @brief Load the encrypted AES key that belongs to the shared file found by id
     * @param id
     * @return byte array encrypted AES key
     */
    public byte[] loadAesKey(int id) {
        String query = "SELECT aesKey FROM SharedFile WHERE id = ?;";
        
        try {
            connect();
            
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);

            preparedStatement.setQueryTimeout(timeout);
            ResultSet results = preparedStatement.executeQuery();
            
            results.next();
            
            byte[] key = results.getBytes("aesKey");   
            return key;
        } catch (SQLException e) {
            ErrorLogger.logError("Error loading AES key from shared file table",e.toString(), false);
        } finally {
            disconnect();
        }
        return null;
    }
    
    /**
     * @brief Updates the AES key for the shared file found by id
     * @param key
     * @param id 
     */
    public void updateAesKey(byte[] key, int id) {
        
        String ddl = "UPDATE SharedFile SET aesKey = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                        
            preparedStatement.setBytes(1, key);
            preparedStatement.setInt(2, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating AES key in shared file table",e.toString(), false);
        } finally {
            disconnect();
        }
    }
    
    /**
     * @brief Updates the directory that this shared file belongs to
     * @param id
     * @param dir 
     */
    public void updateDirectory(int id, int dir) {
        
        String ddl = "UPDATE SharedFile SET directoryID = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                        
            preparedStatement.setInt(1, dir);
            preparedStatement.setInt(2, id);
                        
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
            
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating directory in shared file table",e.toString(), false);
        } finally {
            disconnect();
        }
    }
}
