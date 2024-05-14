/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package security;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import utils.ErrorLogger;

/**
 *
 * @author michael
 */
public class RSA {
    private static KeyFactory keyFactory = getKeyFactory();
    
    
    private static KeyFactory getKeyFactory() {
        try {
           return KeyFactory.getInstance("RSA"); 
        }
        catch(NoSuchAlgorithmException e) {
            ErrorLogger.logError("Failed to generate RSA key factory",e.toString(), false);
        }
        return null;
    }
    
    
    public static byte[] encryptBytes(byte[] bytes, PublicKey publicKey) throws EncryptDecryptException {
        
        try {   
            
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey); 
            
            byte[] encryptedMessageBytes = Base64.getEncoder().encode(encryptCipher.doFinal(bytes));
            return encryptedMessageBytes;
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException  e) {
            throw new EncryptDecryptException(e.toString());
        } 
    }
    
    
    public static byte[] decryptBytes(byte[] bytes, PrivateKey privateKey) throws EncryptDecryptException {
        
        try {            
            
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);  
            
            byte[] decryptedMessageBytes = decryptCipher.doFinal(Base64.getDecoder().decode(bytes));
            return decryptedMessageBytes;
        }
        catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException e) {
            throw new EncryptDecryptException(e.toString());
        }
    }
    
    
    public static PublicKey publicKeyFromBytes(byte[] keyAsBytes) throws EncryptDecryptException {
        try {
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyAsBytes);
            return keyFactory.generatePublic(publicKeySpec); 
        }
        catch (InvalidKeySpecException e) {
            throw new EncryptDecryptException(e.toString());
        }
    }
    
    public static PrivateKey privateKeyFromBytes(byte[] keyAsBytes) throws EncryptDecryptException {
        try {
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyAsBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        }
        catch (InvalidKeySpecException e) {
            throw new EncryptDecryptException(e.toString());
        }
    }
    
}
