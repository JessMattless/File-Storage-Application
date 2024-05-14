/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import com.mycompany.group_9_comp20081_app.App;
import utils.Helper;
import com.mycompany.group_9_comp20081_app.User;
import com.mycompany.group_9_comp20081_app.UserModel;
import static database.DataBase.timeout;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import security.SHA;
import utils.ErrorLogger;

/**
 * @author michael
 */
public class UserTable extends DataBase {
    
    private static UserTable instance = null;
    
    private UserTable(){};
    
    public static UserTable getInstance() {
        if (instance == null) {
            instance = new UserTable();
        }
        return instance;
    }
    
    /**
     * Insert user row into table and return a user instance. Auto generated User ID is returned from insert. 
     * @param name
     * @param password
     * @return user instance
     */
    public User insert(String name, String password) {
        // insert user to table
        String ddl = "INSERT INTO User (name, password, salt, last_login, admin) VALUES ( ?, ?, ?, ?, 0 );";
        
        User user = null;
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl, PreparedStatement.RETURN_GENERATED_KEYS);
            
            String salt = SHA.generateSalt();
            
            String hashedPassword = SHA.hashPassword(password,salt);
            
            String timestamp = Helper.getTimestamp();
            
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, hashedPassword);
            preparedStatement.setString(3, salt);
            preparedStatement.setString(4, timestamp);
           
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
            
            // get the auto generated keys from insert 
            ResultSet keys = preparedStatement.getGeneratedKeys();
            
            // get the id value 
            int id = keys.getInt(1);
            
            // set the id on the user object
            user = new User(id, name, timestamp, 0);

        } catch (SQLException | NullPointerException  e) {
            ErrorLogger.logError("Error inserting into User table",e.toString(), false);
        } finally {
            disconnect();
        }
        
        return user;
    }
    
    /**
     * Insert a default user into the table with admin privileges, used for testing.
     * @return 
     */
    public User insertDefaultAdminUser() {
        String ddl = "INSERT INTO User (name, password, salt, last_login, admin) VALUES ( ?, ?, ?, ?, 1 );";
        
        User user = null;
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl, PreparedStatement.RETURN_GENERATED_KEYS);
            
            String name = "admin";
            
            String salt = SHA.generateSalt();
            
            String hashedPassword = SHA.hashPassword("password", salt);
            
            String timestamp = Helper.getTimestamp();
            
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, hashedPassword);
            preparedStatement.setString(3, salt);
            preparedStatement.setString(4, timestamp);
           
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
            
            // get the auto generated keys from insert 
            ResultSet keys = preparedStatement.getGeneratedKeys();
            
            // get the id value 
            int id = keys.getInt(1);
            
            // set the id on the user object
            user = new User(id, name, timestamp, 1);

        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error inserting default admin user",e.toString(), false);
        } finally {
            disconnect();
        }
        
        return user;
    }
    
    /**
     * Update the privileges of a user to either make them an admin or revoke their admin privileges
     * @param userId
     * @param admin 
     */
    public void updatePrivileges(int userId, int admin) {
        String ddl = "UPDATE User SET admin = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                        
            preparedStatement.setInt(1, admin);
            preparedStatement.setInt(2, userId);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating privileges in User table",e.toString(), false);
        } finally {
            disconnect();
        }
    }
    
    /**
     * Update the password column of row that matches the given id
     * @param id
     * @param password 
     */
    public void updatePassword(int id, String password) {
        String ddl = "UPDATE User SET password = ?, salt = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
            
            String salt = SHA.generateSalt();
            
            String hashedPassword = SHA.hashPassword(password, salt);
                        
            preparedStatement.setString(1, hashedPassword);
            preparedStatement.setString(2, salt);
            preparedStatement.setInt(3, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating password in User table",e.toString(), false);
        } finally {
            disconnect();
        }
    }
    
    /**
     * Update the name column of row that matches the given id
     * @param id
     * @param name 
     */
    public void updateName(int id, String name) {
        
        String ddl = "UPDATE User SET name = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                        
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating name in User table",e.toString(), false);
        } finally {
            disconnect();
        }
    }
    
    /**
     * @brief Update the public key for the user found by id
     * @param id
     * @param publicKey 
     */
    public void updatePublicKey(int id, byte[] publicKey) {
        
        String ddl = "UPDATE User SET publicKey = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                        
            preparedStatement.setBytes(1, publicKey);
            preparedStatement.setInt(2, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating public key in User table",e.toString(), false);
        } finally {
            disconnect();
        }
    }
    
    /**
     * @brief Loads the public RSA that belongs to the user found by id
     * @param id
     * @return byte array containing public RSA key
     */
    public byte[] loadPublicKey(int id) {
        String query = "SELECT publicKey FROM User WHERE id = ?;";
        
        try {
            connect();
            
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);

            preparedStatement.setQueryTimeout(timeout);
            ResultSet results = preparedStatement.executeQuery();
            
            results.next();
            
            byte[] key = results.getBytes("publicKey");   
            return key;
        } catch (SQLException e) {
            ErrorLogger.logError("Error loading public key in User table",e.toString(), false);
        } finally {
            disconnect();
        }
        return null;
    }
    
    /**
     * @brief Loads the admin privileges for the user found by id
     * @param id
     * @return int user privilege
     */
    public int loadPrivileges(int id) {
        String query = "SELECT admin FROM User WHERE id = ?;";
        
        try {
            connect();
            
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);

            preparedStatement.setQueryTimeout(timeout);
            ResultSet results = preparedStatement.executeQuery();
            
            results.next();
            
            int privilege = results.getInt("admin");   
            return privilege;
        } catch (SQLException e) {
            ErrorLogger.logError("Error loading privileges in User table",e.toString(), false);
        } finally {
            disconnect();
        }
        return -1;
    }
    
    /**
     * Update the timestamp of last login for given user id
     * @param id
     * @param timestamp 
     */
    public void updateLastLogin(int id, String timestamp) {
        
        String ddl = "UPDATE User SET last_login = ? WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
                        
            preparedStatement.setString(1, timestamp);
            preparedStatement.setInt(2, id);
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error updating last login in User table",e.toString(), false);
        } finally {
            disconnect();
        }
    }
    
    /**
     * Delete row corresponding to this user from the database.
     * @param user 
     */
    public void delete(User user) {
        // update table data with user data
        
        String ddl = "DELETE FROM User WHERE id = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(ddl);
            
            preparedStatement.setInt(1, user.getId());
            
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.executeUpdate();
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error deleting User row",e.toString(), false);
        } finally {
            disconnect();
        }
    }
    
    /**
     * @brief Checks the username and password. Returns users id if successful and -1 if unsuccessful.
     * @param name
     * @param password
     * @return int id
     */
    public int userLogin(String name, String password) {
        
        if (name.length() == 0|| password.length() == 0) {
            return -1;
        }
        
        String query = "SELECT id,password,salt FROM User WHERE name = ?;";
        
        try {          
            connect();
            var preparedStatement = connection.prepareStatement(query);
            
            preparedStatement.setString(1, name);
            preparedStatement.setQueryTimeout(timeout);
            
            ResultSet results = preparedStatement.executeQuery();
            
            String storedPassword = results.getString("password");
            String salt = results.getString("salt");
            int userId = results.getInt("id");
            
            String hashedEnteredPassword = SHA.hashPassword(password, salt);
            
            if (hashedEnteredPassword.equals(storedPassword)) {
                return userId;
            }
            
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error getting login from User table",e.toString(), false);
        } finally {
            disconnect();
        }
        
        return -1;
    }
    
    
    /**
     * Attempts to find user in table for given name and password. Will return null if not found.
     * @param name
     * @param password
     * @return User object
     */
    public User loadUser(String name, String password) {
        // null is returned if no user is found
        User user = null;
        
        int userId = userLogin(name,password);
        
        if (userId == -1) {
            return null;
        }
        
        String query = "SELECT * FROM User WHERE id = ?;";
        
        try {
            connect();
            var preparedStatement = connection.prepareStatement(query);
            
            preparedStatement.setInt(1, userId);
            preparedStatement.setQueryTimeout(timeout);
            
            ResultSet results = preparedStatement.executeQuery();
            
            results.next();
            
            String uname = results.getString("name");
            String lastLogin = results.getString("last_login");
            int admin = results.getInt("admin");
            
            user = new User(results.getInt("id"), uname, lastLogin, admin);
                         
        } catch (SQLException e) {
            ErrorLogger.logError("Error loading User by name and password",e.toString(), false);
        } finally {
            disconnect();
        }
     
        return user;
    }
    
    /**
     * Use a user id and device id to log in a user automatically on startup
     * @param userID
     * @param deviceID
     * @return User object
     */
    public User loadUser(int userID) {
        // null is returned if no user is found
        User user = null;
        
        try {
            connect();
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet results = statement.executeQuery("SELECT * FROM User;");

            while (results.next()) {
                String uname = results.getString("name");
                String lastLogin = results.getString("last_login");
                int id = results.getInt("id");
                int admin = results.getInt("admin");
                
                if (id == userID) {
                    user = new User(id, uname, lastLogin, admin);
                    break;
                }
            }
        } catch (SQLException e) {
            ErrorLogger.logError("Error loading User by ID",e.toString(), true);
        } finally {
            disconnect();
        }
     
        return user;
    }
    
    /**
     * Return the hashed password for the given user object
     * @param user 
     */
    public String loadPassword(User user) {
        // update table data with user data
        
        String query = "SELECT password FROM User WHERE id = ?;";
        String pass = null;
        try {          
            connect();
  
            var preparedStatement = connection.prepareStatement(query);
            
            preparedStatement.setInt(1, user.getId());
            
            preparedStatement.setQueryTimeout(timeout);
            ResultSet results = preparedStatement.executeQuery();
            
            if (results.next()) {
                pass = results.getString("password");
            }
            
        } catch (SQLException | NullPointerException e) {
            ErrorLogger.logError("Error loading Users password from User table",e.toString(), true);
        } finally {
            disconnect();
        }
        
        return pass;
    }
    
    /**
     * Check if a user exists
     * @param name
     * @return boolean
     */
    public boolean exists(String name) {
        
        // if query fails then it is safer to assume user exists
        boolean exists = true;

        String query = "SELECT * FROM User WHERE name = ?;";
        
        try {
            connect();
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setQueryTimeout(timeout);
            preparedStatement.setString(1, name);
            ResultSet results = preparedStatement.executeQuery();
            
            exists = results.next();
        } catch (SQLException e) {
            ErrorLogger.logError("Error checking User exists in User table",e.toString(), false);
        } finally {
            disconnect();
        }
        
        return exists;
    }

    
    /**
     * @brief Get a list of all users that are not the current user. 
     * User instances are returned as UserModel objects so they can be used in a table.
     * @return ArrayList of UserModel 
     */
    public ArrayList<UserModel> getUsers() {
        return getUsers("_");
    }
    
    /**
     * @brief Get a filtered list of all users that are not the current user. 
     * User instances are returned as UserModel objects so they can be used in a table.
     * @param filter Filters on name and id
     * @return ArrayList of UserModel 
     */
    public ArrayList<UserModel> getUsers(String filter) {
        
        ArrayList<UserModel> users = new ArrayList<>();
        User currentUser = App.getCurrentUser();
        
        try {
            connect();
            
            String query = "SELECT id,name FROM User WHERE (name LIKE ? OR id = ?) AND id <> ? ORDER BY name;";
            
            var statement = connection.prepareStatement(query);
            
            String nameSearch = "%" + filter + "%";
            
            statement.setString(1, nameSearch);
            statement.setString(2, filter);
            statement.setInt(3, currentUser.getId());
            
            statement.setQueryTimeout(timeout);
            
            ResultSet results = statement.executeQuery();      
            
            while (results.next()) {
                int id = results.getInt("id");
                String name = results.getString("name");
                
                UserModel user = new UserModel(id, name);
                users.add(user);
            }
        } catch (SQLException e) {
            ErrorLogger.logError("Error loading list of Users from table",e.toString(), false);
        } finally {
            disconnect();
        }            
        return users;
    }
}
