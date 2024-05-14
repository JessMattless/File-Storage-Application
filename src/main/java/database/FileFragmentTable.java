/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import utils.ErrorLogger;

/**
 * Stores the location of file fragments that make up user files
 * References the stored file table 
 * Singleton Class
 * @author michael
 */
public class FileFragmentTable extends DataBase {
    
    private static FileFragmentTable instance = null;
    
    private FileFragmentTable(){};
    
    /**
     * Get the one instance of this class
     * @return FileFragmentTable object
     */
    public static FileFragmentTable getInstance() {
        if (instance == null) {
            instance = new FileFragmentTable();
        }
        return instance;
    }
    
    /**
     * Insert row into table and return a boolean if no errors
     * @param fileId
     * @param index
     * @param location
     * @return boolean successful insert
     */
    public boolean insert(int fileId, int index, String name) {
        
        // insert user to table
        String ddl = "INSERT INTO FileFragment (fileId, frag_index, location, container) VALUES ( ?, ?, ?, ? );";
        
        boolean inserted = false;
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                                  
            preparedStatement.setInt(1, fileId);
            preparedStatement.setInt(2, index);
            preparedStatement.setString(3, name);
            preparedStatement.setInt(4, index);
           
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
            inserted = true;
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error inserting file fragment row",e.toString(), false);
        } finally {
            disconnect();
        }
        return inserted;
    }
     
    /**
     * Returns an arraylist of fragments that relate to the file for given ID. <br>
     * Fragments are returned in the correct order they need to be assembled in.
     * @param fileId
     * @return ArrayList of Path objects
     */
    public ArrayList<String> getFragments(int fileId) {
        ArrayList<String> fileFragmentData = new ArrayList<>();
        
        String query = "SELECT location FROM FileFragment WHERE fileId = ? ORDER BY frag_index;";
        
        try {
            connect();
            var statement = connection.prepareStatement(query);
            statement.setInt(1, fileId);
            statement.setQueryTimeout(timeout);
            ResultSet results = statement.executeQuery();
            
            while (results.next()) {
                fileFragmentData.add(results.getString("location"));
            }
        } catch (SQLException e) {
            ErrorLogger.logError("Error getting file fragments from table",e.toString(), false);
        } finally {
            disconnect();
        }
        return fileFragmentData;
    }
    
    /**
     * Delete all rows that relate to the given file id
     * @param fileId 
     */
    public void deleteRelatedFragments(int fileId) {      
        String dml = "DELETE FROM FileFragment WHERE fileId = ?;";     
        try {
            connect();
            var statement = connection.prepareStatement(dml);
            statement.setInt(1, fileId);
            statement.setQueryTimeout(timeout);
            statement.executeUpdate();   
        } catch (SQLException e) {
            ErrorLogger.logError("Error deleting file fragment rows",e.toString(), false);
        } finally {
            disconnect();
        }
    }

}
