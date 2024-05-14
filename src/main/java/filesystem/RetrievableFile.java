/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package filesystem;

import java.io.File;
import security.EncryptDecryptException;

/**
 *
 * @author michael
 */
public interface RetrievableFile {
    public File retrieve(String path) throws EncryptDecryptException ;
}
