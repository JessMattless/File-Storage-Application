/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import javafx.scene.control.Alert;
import utils.ErrorLogger;


/**
 * @author michael
 * Superclass for all database table classes. Handles database connection.
 */
public class DataBase {
    
    private static final String URL = "jdbc:sqlite:comp20081.db";
    protected Connection connection = null;
    static int timeout = 30;
    
    public DataBase() {}
    
    public void createTables() {
        
        StringBuilder sb = new StringBuilder();
        
        File sqlFile = new File("target/classes/sql/createTables.sql");
        try {
            Scanner scanner = new Scanner(sqlFile);
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
        } catch(FileNotFoundException e) {
            ErrorLogger.logError("Error loading createTables.sql file",e.toString(), false);
        }

        try {
            connect();
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate(sb.toString());

        } catch (SQLException e) {
            ErrorLogger.logError("Error creating database tables",e.toString(), true);
        } finally {
            disconnect();
        }
    }

    
    /**
     * Connect to the database.
     */
    public void connect() {
        try {
            if (connection == null){
                connection = DriverManager.getConnection(URL);
            }
        } catch (SQLException e) {
            ErrorLogger.logError("Error connecting to database",e.toString(), true);
        }
    }
    
    /**
     * Close connection to the database.
     */
    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            ErrorLogger.logError("Error disconnecting from database",e.toString(), true);
        }
    }
}
