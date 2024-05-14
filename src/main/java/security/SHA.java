/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import utils.ErrorLogger;

/**
 *
 * @author michael
 */
public class SHA {
    
    private static final String SALTCHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ITERATIONS = 10000;
    private static final int KEYLENGTH = 256;
    private static final int SALTLENGTH = 32;

    
    public static String generateSalt() {
        StringBuilder saltValue = new StringBuilder(SALTLENGTH);

        Random random = new SecureRandom();
        
        int saltCharsLength = SALTCHARS.length();
        
        for (int i = 0; i < SALTLENGTH; i++) {
            int randomInt = random.nextInt(saltCharsLength);
            char nextChar = SALTCHARS.charAt(randomInt);
            saltValue.append(nextChar);
        }

        return saltValue.toString();
    }
    
    public static String hashPassword(String password, String salt) {
        String hashedPassword = null;
        try { 
            byte[] securePassword = _hash(password.toCharArray(), salt.getBytes());
            hashedPassword = Base64.getEncoder().encodeToString(securePassword);
        } catch(AssertionError | InvalidKeySpecException e) {
            ErrorLogger.logError("Failed to hash password",e.toString(), false);
        } 
        return hashedPassword;
    }
    
    /* Method to generate the hash value */
    private static byte[] _hash(char[] password, byte[] salt) throws InvalidKeySpecException {
        // get key spec
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, ITERATIONS, KEYLENGTH);
        // set whole password char array to minimum char value
        Arrays.fill(password, Character.MIN_VALUE);
       
        try {
            // create instance of secret key factory
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            // generate secret from factory
            SecretKey secret = skf.generateSecret(keySpec);
            // return encoded secret
            return secret.getEncoded();
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing value: " + e.getMessage(), e);      
        } finally {
            keySpec.clearPassword();
        }
    }
    
}
