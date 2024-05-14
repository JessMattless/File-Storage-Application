/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import com.mycompany.group_9_comp20081_app.User;
import filesystem.StoredFile;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import utils.ErrorLogger;

/**
 * @author michael
 */
public class FileTable extends DataBase {
    
    private static FileTable instance = null;
    
    private FileTable(){};
    
    public static FileTable getInstance() {
        if (instance == null) {
            instance = new FileTable();
        }
        return instance;
    }
   
    /**
     * @brief Inserts a row into the stored file table and returns an instance of StoredFile that corresponds to the row
     * @param user
     * @param name
     * @param dir
     * @param readOnly
     * @param aesKey
     * @return StoredFile object
     */
    public StoredFile insert(int user, String name, int dir, boolean readOnly, byte[] aesKey) {
        StoredFile file = null;
        
        String ddl = "INSERT INTO StoredFile (owner, name, directoryID, readOnly, aesKey) VALUES ( ?, ?, ?, ?, ? );";
            
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl, PreparedStatement.RETURN_GENERATED_KEYS);
                                  
            preparedStatement.setInt(1, user);
            preparedStatement.setString(2, name);
            preparedStatement.setInt(3, dir);
            preparedStatement.setBoolean(4, readOnly);
            preparedStatement.setBytes(5, aesKey);
           
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
            
            // get the auto generated keys from insert 
            ResultSet keys = preparedStatement.getGeneratedKeys();
            
            // get the id value 
            int key = keys.getInt(1);
            file = new StoredFile(key, name, dir);
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error inserting stored file table row",e.toString(), true);
        } finally {
            disconnect();
        }
        return file;
    }
    
    /**
     * Update the name attribute of the file row that matches the given id
     * @param id
     * @param name 
     */
    public void updateName(int id, String name) {
        
        String ddl = "UPDATE StoredFile SET name = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                        
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating name in stored file table",e.toString(), true);
        } finally {
            disconnect();
        }
    }
    
    public void updateDirectory(int id, int dir) {
        
        String ddl = "UPDATE StoredFile SET directoryID = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                        
            preparedStatement.setInt(1, dir);
            preparedStatement.setInt(2, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating directory in stored file table",e.toString(), true);
        } finally {
            disconnect();
        }
    }
    
    /**
     * @brief Deletes the row in the stored file table with the given id
     * @param id 
     */
    public void delete(int id) {
        
        String dml = "DELETE FROM StoredFile WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(dml);
            
            preparedStatement.setInt(1, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error deleting stored file table row",e.toString(), true);
        } finally {
            disconnect();
        }
    }
    
    /**
     * @brief Load an instance of StoredFile that corresponds to the given id
     * @param id
     * @return StoredFile
     */
    public StoredFile loadFile(int id) {
        StoredFile file = null;
        
        String query = "SELECT * FROM StoredFile WHERE id = ?;";
        
        try {
            connect();
            
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);

            preparedStatement.setQueryTimeout(timeout);
            ResultSet results = preparedStatement.executeQuery();
            
            results.next();
            
            int fileId = results.getInt("id");
            String name = results.getString("name");
            int parentDir = results.getInt("directoryID");
            int ownerId = results.getInt("owner");
            boolean readOnly = results.getBoolean("readOnly");
            file = new StoredFile(results.getInt("id"), name, parentDir, ownerId, readOnly);
                                  
        } catch (SQLException e) {
            ErrorLogger.logError("Error loading stored file table row",e.toString(), true);
        } finally {
            disconnect();
        }
        return file;
    }
    
    /**
     * @brief Load the encrypted AES key that belongs to the stored file found by id
     * @param id
     * @return byte array containing encrypted AES key
     */
    public byte[] loadAesKey(int id) {
        String query = "SELECT aesKey FROM StoredFile WHERE id = ?;";
        
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
            ErrorLogger.logError("Error loading AES key for stored file",e.toString(), false);
        } finally {
            disconnect();
        }
        return null;
    }
    
    /**
     * @brief Updates the AES key for the stored file found by id
     * @param key
     * @param id 
     */
    public void updateAesKey(byte[] key, int id) {
        
        String ddl = "UPDATE StoredFile SET aesKey = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                        
            preparedStatement.setBytes(1, key);
            preparedStatement.setInt(2, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating AES key for stored file",e.toString(), false);
        } finally {
            disconnect();
        }
    }
    
    
    /**
     * Return an ArrayList of StoredFile objects that are in the users root directory
     * @param userId
     * @return ArrayList of StoredFile objects
     */
    public ArrayList<StoredFile> getUserFiles(int userId) {            
        return getUserFiles(userId, 0);
    }
    
    /**
     * Return an ArrayList of StoredFile objects that belong to the given user in the specified directory
     * @param userId
     * @param dir
     * @return ArrayList of StoredFile objects
     */
    public ArrayList<StoredFile> getUserFiles(int userId, int dir) {
        
        ArrayList<StoredFile> files = new ArrayList<>();
        
        try {
            connect();
            
            String query = "SELECT * FROM StoredFile WHERE owner = ? AND directoryID = ? ORDER BY name;";
            
            var statement = connection.prepareStatement(query);
            
            statement.setInt(1, userId);
            statement.setInt(2, dir);
            
            statement.setQueryTimeout(timeout);
            
            ResultSet results = statement.executeQuery();      
            
            while (results.next()) {
                int fileId = results.getInt("id");
                String filename = results.getString("name");
                int parentDir = results.getInt("directoryID");
                int ownerId = results.getInt("owner");
                boolean readOnly = results.getBoolean("readOnly");
                
                StoredFile file = new StoredFile(fileId,filename, parentDir, ownerId, readOnly);
                files.add(file);
            }
        } catch (SQLException e) {
            ErrorLogger.logError("Error loading users stored files from table",e.toString(), true);
        } finally {
            disconnect();
        }            
        return files;
    }
    
    /**
     * @brief Get the user instance that owns this file.
     * @param id
     * @return User
     */
    public User GetFileOwner(int id) {
        // null is returned if no user is found
        User user = null;
        
        String query = "SELECT U.* FROM User U " +
                    "JOIN StoredFile F ON F.owner = U.id " +
                    "WHERE U.id = ?;";
        
        try {
            connect();
            var statement = connection.prepareStatement(query);
            statement.setQueryTimeout(timeout);
            
            statement.setInt(1, id);
            
            ResultSet results = statement.executeQuery();
                  
            results.next();
            String uname = results.getString("name");
            String pass = results.getString("password");
            String lastLogin = results.getString("last_login");
            int userId = results.getInt("id");
            int admin = results.getInt("admin");
            user = new User(userId, uname, lastLogin, pass, admin);
            
        } catch (SQLException e) {
            ErrorLogger.logError("Error getting file owner from table",e.toString(), false);
        } finally {
            disconnect();
        }
        return user;
    }
}
