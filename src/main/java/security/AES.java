/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package security;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import utils.ErrorLogger;

/**
 *
 * @author michael
 */
public class AES {
    private static final int AES_KEYLENGTH = 256;
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEYFACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int IV_LENGTH = 16;
   
    public static SecretKey generateKey() {
       
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEYLENGTH);
            SecretKey key = keyGenerator.generateKey();
            return key;
        }
        catch(NoSuchAlgorithmException e) {
            ErrorLogger.logError("Error generating AES key",e.toString(), false);
        }    
        return null;
    }
    
    public static SecretKey getKeyFromPassword(String password, String salt)
        throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYFACTORY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret;
    }
    
    
    public static byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
    
    
    public static void encryptFile(File fileIn, File fileOut, SecretKey key) throws EncryptDecryptException {
        File output = new File("encrypted");
        
        try{
            byte[] fileAsBytes = Files.readAllBytes(fileIn.toPath());
            
            byte[] iv = generateIv();
            
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            
            byte[] encrypted =  _encrypt(fileAsBytes, key, ivParameterSpec);
            
            byte[] dataPacket = new byte[encrypted.length + IV_LENGTH];
            
            for (int i = 0; i < IV_LENGTH; i++) {
                dataPacket[i] = iv[i];
            }
            
            for (int i = 0; i < encrypted.length; i++) {
                dataPacket[i + IV_LENGTH] = encrypted[i];
            }
            
            FileOutputStream out = new FileOutputStream(fileOut);
            
            out.write(dataPacket);
            
            out.close();
            
        }
        catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e ) {
            throw new EncryptDecryptException(e.toString());
        }
    }
    
    
    public static ByteArrayInputStream encryptStream(ByteArrayInputStream inputStream, SecretKey key) throws EncryptDecryptException {
        
        try{
            byte[] fileAsBytes = inputStream.readAllBytes();
            
            byte[] iv = generateIv();
            
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            
            byte[] encrypted =  _encrypt(fileAsBytes, key, ivParameterSpec);
            
            byte[] dataPacket = new byte[encrypted.length + IV_LENGTH];
            
            for (int i = 0; i < IV_LENGTH; i++) {
                dataPacket[i] = iv[i];
            }
            
            for (int i = 0; i < encrypted.length; i++) {
                dataPacket[i + IV_LENGTH] = encrypted[i];
            }
            
            ByteArrayInputStream encryptedStream = new ByteArrayInputStream(dataPacket);

            return encryptedStream;
        }
        catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e ) {
            throw new EncryptDecryptException(e.toString());
        }
    }
    
    
     public static void decryptFile(File fileIn, File fileOut, SecretKey key) throws EncryptDecryptException {
        
        try{
            byte[] fileAsBytes = Files.readAllBytes(fileIn.toPath());
            byte[] iv = new byte[16];
            byte[] fileData = new byte[fileAsBytes.length - 16];
            
            for (int i = 0; i < 16; i++) {
                iv[i] = fileAsBytes[i];
            }
            
            for (int i = 16; i < fileAsBytes.length; i++) {
                fileData[i-16] = fileAsBytes[i];
            }
                        
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            
            byte[] decrypted = _decrypt(fileData, key, ivParameterSpec);
            
            FileOutputStream out = new FileOutputStream(fileOut);
            
            out.write(decrypted);
            
        }
        catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e ) {
            throw new EncryptDecryptException(e.toString());
        }
    }
    
     
    public static ByteArrayInputStream decryptStream(ByteArrayInputStream inputStream, SecretKey key) throws EncryptDecryptException {
        
        try{
            byte[] fileAsBytes = inputStream.readAllBytes();
            byte[] iv = new byte[16];
            byte[] fileData = new byte[fileAsBytes.length - 16];
            
            for (int i = 0; i < 16; i++) {
                iv[i] = fileAsBytes[i];
            }
            
            for (int i = 16; i < fileAsBytes.length; i++) {
                fileData[i-16] = fileAsBytes[i];
            }
                        
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            
            byte[] decrypted = _decrypt(fileData, key, ivParameterSpec);
            
            ByteArrayInputStream decryptedStream = new ByteArrayInputStream(decrypted);
            
            return decryptedStream;
        }
        catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e ) {
            throw new EncryptDecryptException(e.toString());
        }
    }
    
    
    private static byte[] _encrypt(byte[] input, SecretKey key, IvParameterSpec iv) 
        throws NoSuchPaddingException, NoSuchAlgorithmException,
        InvalidAlgorithmParameterException, InvalidKeyException,
        BadPaddingException, IllegalBlockSizeException {
    
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherBytes = cipher.doFinal(input);
        return cipherBytes;
    }
    
    private static byte[] _decrypt(byte[] input, SecretKey key, IvParameterSpec iv) 
        throws NoSuchPaddingException, NoSuchAlgorithmException,
        InvalidAlgorithmParameterException, InvalidKeyException,
        BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainBytes = cipher.doFinal(input);
        return plainBytes;
    }
}
