/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import static database.DataBase.timeout;
import filesystem.Directory;
import filesystem.FileSystemObject;
import filesystem.StoredFile;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.scene.control.Alert;
import utils.ErrorLogger;
import utils.Helper;

/**
 *
 * @author michaels
 */
public class DeleteScheduleTable extends DataBase {
    private static DeleteScheduleTable instance = null;
    
    private DeleteScheduleTable(){};
    
    public static DeleteScheduleTable getInstance() {
        if (instance == null) {
            instance = new DeleteScheduleTable();
        }
        return instance;
    }
    
    /**
     * @brief Inserts a row into the delete schedule table. Automatically calculates the delete schedule timestamp.
     * @param itemType
     * @param id
     * @param restoreLoc
     * @return boolean insert successful
     */
    public boolean insert(String itemType, int id, int restoreLoc) {
        boolean inserted = false;
        String ddl = "INSERT INTO DeleteSchedule (storedItemType, itemID, restoreLocation, deleteWhen) VALUES ( ?, ?, ?, ? );";
            
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
            
            String deleteWhen = Helper.getTimestamp(30);
                                              
            preparedStatement.setString(1, itemType);
            preparedStatement.setInt(2, id);
            preparedStatement.setInt(3, restoreLoc);
            preparedStatement.setString(4, deleteWhen);
           
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
            inserted = true;
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error inserting delete schedule row",e.toString(), false);
        } finally {
            disconnect();
        }
        return inserted;
    }
    
    /**
     * @brief Returns a list of all file system objects that are now scheduled for deletion
     * @return ArrayList<FileSystemObject>
     */
    public ArrayList<FileSystemObject> scheduledForDeletion() {
        ArrayList<FileSystemObject> itemsToDelete = new ArrayList<>();
        
        try {
            connect();
                        
            String query =  "SELECT id, name, storedItemType FROM Directory\n" +
                            "JOIN DeleteSchedule ON itemID = id\n" +
                            "WHERE storedItemType = 'DIR'\n" +
                            "AND deleteWhen < datetime('now')\n" +
                            "UNION\n" +
                            "SELECT id, name, storedItemType FROM StoredFile\n" +
                            "JOIN DeleteSchedule ON itemID = id\n" +
                            "WHERE storedItemType = 'FILE'\n" +
                            "AND deleteWhen < datetime('now');";
            
            var statement = connection.createStatement();
            
            statement.setQueryTimeout(timeout);
            
            ResultSet results = statement.executeQuery(query);      
            
            while (results.next()) {
                String itemType = results.getString("storedItemType");
                int id = results.getInt("id");
                String name = results.getString("name");
                
                if (itemType.equals("DIR")) {
                    itemsToDelete.add(new Directory(id,name));
                }
                else {
                    itemsToDelete.add(new StoredFile(id,name,-2));
                }

            }
        } catch (SQLException e) {
            ErrorLogger.logError("Error getting delete schedule rows",e.toString(), false);
        } finally {
            disconnect();
        }       
        
        return itemsToDelete;
    }
    
    /**
     * @brief Returns the id of the directory the file object was deleted from. <br>
     * If location no longer exists then returns id of home directory
     * @param itemType
     * @param id
     * @return int directory id
     */
    public int loadRestoreLocation(String itemType, int id) {
        int restoreDirID = 0;
        
        String query = "SELECT restoreLocation FROM DeleteSchedule WHERE storedItemType = ? AND itemID = ?;";
            
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(query);
            
            preparedStatement.setString(1, itemType);
            preparedStatement.setInt(2, id);

            preparedStatement.setQueryTimeout(timeout);
            ResultSet results = preparedStatement.executeQuery();
            
            if (results.next()) {
                restoreDirID = results.getInt("restoreLocation");
            }
            
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error getting deleted file restore location",e.toString(), true);
        } finally {
            disconnect();
        }
        return restoreDirID;
    }
    
    /**
     * @brief Deletes the row from the delete schedule for the given file id
     * @param type
     * @param id 
     */
    public void delete(String type, int id) {
        
        String dml = "DELETE FROM DeleteSchedule WHERE storedItemType = ? AND itemID = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(dml);
            
            preparedStatement.setString(1, type);
            preparedStatement.setInt(2, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error deleting delete schedule row",e.toString(), false);
        } finally {
            disconnect();
        }
    }
}
