/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filesystem;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.mycompany.group_9_comp20081_app.App;
import com.mycompany.group_9_comp20081_app.User;
import database.DeleteScheduleTable;
import database.FileFragmentTable;
import database.FileTable;
import database.SharedFileTable;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import security.AES;
import security.EncryptDecryptException;
import security.RSA;
import utils.ErrorLogger;

/**
 *
 * @author michael
 */
public class SecureStorage {
    
    private static final int STORAGE_NODES = 4;
    private static String file_storage;
    private static ArrayList<StorageContainer> storageContainers = new ArrayList<>();
    
    private static final String STORAGE_USERNAME = "root";
    private static final String STORAGE_PASSWORD = "Admin1";
    
    public static StoredFile store(String fileName, File file, int parentDir) {
        return store(fileName,file,parentDir,App.getCurrentUser());
    }
    
    /**
     * Store the given file securely in separate directories. <br>
     * @param file
     * @return 
     */
    public static StoredFile store(String fileName, File file, int parentDir, User user) {        
        FileTable fileTable = FileTable.getInstance();
        
        boolean readOnly = !file.canWrite();
        
        SecretKey key = AES.generateKey();
        
        try {
            byte[] encryptedKey = RSA.encryptBytes(key.getEncoded(), user.getPublicKey());
        
            StoredFile storedFile = fileTable.insert(user.getId(), fileName, parentDir, readOnly, encryptedKey);
            
            ByteArrayInputStream plainTextStream = new ByteArrayInputStream(Files.readAllBytes(file.toPath()));
            
            ByteArrayInputStream encryptedByteStream = AES.encryptStream(plainTextStream, key);
            
            ArrayList<String> chunks = storeFileAsChunks(encryptedByteStream, user.getId());
            
            plainTextStream.close();
            encryptedByteStream.close();
            
            FileFragmentTable fragTable = FileFragmentTable.getInstance();
            
            int chunkIndex = 0;
            for (String chunk : chunks) {
                fragTable.insert(storedFile.getId(), chunkIndex++, chunk);
            }
            
            return storedFile;
        }
        catch (IOException | EncryptDecryptException e) {
            ErrorLogger.logError("Failed to store file",e.toString(), true);
        }
                
        return null;
    }
    
    
    /**
     * @brief Retrieves a file from secure storage and outputs it to the given file path. <br>
     * Decrypts the file AES key using the users private RSA key.
     * @param path destination
     * @param fileId
     * @return File
     */
    public static File retrieveFile(String path, int fileId) throws EncryptDecryptException {
        User user = App.getCurrentUser();
        FileTable fileTable = FileTable.getInstance();
        byte[] decryptedKey = RSA.decryptBytes(fileTable.loadAesKey(fileId), user.getPrivateKey());
        return _retrieve(path,fileId,decryptedKey);
    }
    
    /**
     * @brief Retrieves a file that has been shared with this user from secure storage and outputs it to the given file path.
     * Decrypts the shared file AES key using the users private RSA key.
     * @param path destination
     * @param fileId
     * @return File
     */
    public static File retrieveSharedFile(String path, int sharedFileId, int fileId) throws EncryptDecryptException {
        User user = App.getCurrentUser();
        SharedFileTable sharedFileTable = SharedFileTable.getInstance();
        byte[] decryptedKey = RSA.decryptBytes(sharedFileTable.loadAesKey(sharedFileId), user.getPrivateKey());
        return _retrieve(path,fileId,decryptedKey);
    }
       
    /**
     * @brief Retrieves the whole file from storage and decrypts it using the AES key.
     * @param path
     * @param fileId
     * @param aesKeyBytes
     * @return File
     */
    private static File _retrieve(String path, int fileId, byte[] aesKeyBytes){
        // output file object
        try {
            SecretKey key = new SecretKeySpec(aesKeyBytes, "AES");
            File file = new File(path);
            file.createNewFile();
            
            assembleChunksIntoFile(file, fileId);
            
            ByteArrayInputStream encryptedStream = new ByteArrayInputStream(Files.readAllBytes(file.toPath()));
            
            ByteArrayInputStream plainTextStream = AES.decryptStream(encryptedStream, key);
            
            byte[] buffer = plainTextStream.readAllBytes();
            
            OutputStream fileOut = new FileOutputStream(file);
            fileOut.write(buffer);
            
            encryptedStream.close();
            plainTextStream.close();
            fileOut.close();
                    
            return file;
        }
        catch (IOException | EncryptDecryptException e) {
            ErrorLogger.logError("Could not retrieve file",e.toString(), true);
        }
        return null;
    }
    
    
    /**
     * @brief Loads the file chunks from storage and assembles them. The whole file is still encrypted at this point.
     * @param file Assemble the chunks into this file
     * @param fileId
     */
    private static void assembleChunksIntoFile(File file, int fileId) {
        
        FileFragmentTable fragTable = FileFragmentTable.getInstance();
        
        ArrayList<String> fragmentLocations = fragTable.getFragments(fileId);
        
        try {
            // will write the data from file chunks to the output file
            OutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
            
            int containerId = 0;
            for (String fragmentLocation : fragmentLocations ) {
                
                StorageContainer container = storageContainers.get(containerId++);

                byte[] chunkData = container.downloadFile(fragmentLocation);
                
                writer.write(chunkData);
                
                writer.flush();
            }
            writer.close();
        } catch(IOException | JSchException | SftpException e) {
            ErrorLogger.logError("Failed to assemble file chunks",e.toString(), true);
        }
    }

    
    private static ArrayList<String> storeFileAsChunks(ByteArrayInputStream inputStream, int userId) {
        ArrayList<String> fileChunks = new ArrayList<>();
        
        try {
            // convert the file to a byte array
            byte[] fileAsBytes = inputStream.readAllBytes();
            
            long fileSize = fileAsBytes.length;
            // ceiling the chunk size so we dont lose data
            int chunkSize = (int) fileSize / STORAGE_NODES + 1;
            
            int byteOffset = 0;
            
            for (int i = 0; i < 4; i++ ){    
                                
                String uniqueFilename = UUID.randomUUID().toString();
                String fragmentLocation = Path.of("/user_files/", String.valueOf(userId), uniqueFilename).toString();
                
                InputStream chunkInputStream = new ByteArrayInputStream(fileAsBytes, byteOffset, chunkSize);    

                byteOffset += chunkSize;

                fileChunks.add(fragmentLocation);
                
                try {
                    storageContainers.get(i).uploadFile(chunkInputStream, fragmentLocation);
                }
                catch (JSchException | SftpException e) {
                    ErrorLogger.logError("Error uploading file chunk to container",e.toString(), true);
                }
                
                chunkInputStream.close();
            }
            
            inputStream.close();
            
        } catch(Exception e) {
            ErrorLogger.logError("Error splitting file into chunks",e.toString(), true);
        }
        
        return fileChunks;
    }
    
    
    /**
      * Delete file chunks that are in storage and delete rows in the database that relate to them and this stored file.
      */
    public static void delete(int fileId) {
        FileFragmentTable fileFragTable = FileFragmentTable.getInstance();
        ArrayList<String> fileFragmentLocations = fileFragTable.getFragments(fileId);
        
        int containerId = 0;
        for (String fragmentLocation : fileFragmentLocations) {
            StorageContainer container = storageContainers.get(containerId++);
            try {
                container.deleteFile(fragmentLocation);
            }
            catch (JSchException | SftpException e) {
                ErrorLogger.logError("Error deleting file chunks",e.toString(), true);
            }
        }
       
        fileFragTable.deleteRelatedFragments(fileId);
        FileTable fileTable = FileTable.getInstance();
        fileTable.delete(fileId);
        // remove row delete schedule table that relates to this file
        DeleteScheduleTable.getInstance().delete("FILE", fileId);
    }
    
    
    /**
     * Create the storage containers for file chunks
     */
    public static void createFileStorage(String location) {
        
        for (int i = 0; i < 4; i++) {
            String host = "172.18.0." + String.valueOf(2 + i);
            StorageContainer storageContainer = new StorageContainer(STORAGE_USERNAME,STORAGE_PASSWORD,host);
            storageContainers.add(storageContainer);
            
            try {
                storageContainer.connect();
            }
            catch (JSchException e) {
                ErrorLogger.logError("Error connecting to file storage container",e.toString(), true);
            }
        }
    }
    
    public static StorageContainer getContainer(int containerNumber) {
        return storageContainers.get(containerNumber);
    }
    
    public static ArrayList<StorageContainer> getContainerList() {
        return storageContainers;
    }
}
