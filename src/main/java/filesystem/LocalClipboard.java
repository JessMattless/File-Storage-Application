/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filesystem;

/**
 *
 * @author michael
 */
public class LocalClipboard {
    private static FileSystemObject fileObject;
    private static boolean copied = true;
    
    public static void copy(FileSystemObject fileObject) {
        LocalClipboard.fileObject = fileObject;
        copied = true;
    }
    
    public static void cut(FileSystemObject fileObject) {
        LocalClipboard.fileObject = fileObject;
        copied = false;
    }
    
    public static void paste(Directory location) {
        if (copied) {
            fileObject.copy(location);
        }
        else {
            fileObject.move(location);
            fileObject = null;
        }
    }

    public static boolean hasFile() {
        return (fileObject != null);
    }
    
}
