/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author jakem
 */
public final class FileHandler { 
    public static void createFile(String fileName) {
        try {
            File f = new File(fileName);
            
            if (!f.createNewFile())
                System.out.println("File already exists.");
        }
        catch (IOException e) {
            ErrorLogger.logError("Error creating file", e.toString(), false);
        }
    }
    
    public static List<String> readFile(String fileName) {
        List<String> fileLines = new ArrayList<>();
        File f = new File(fileName);
        if (f.exists()) {
            try (Scanner scanner = new Scanner(f)){
                while (scanner.hasNextLine()) {
                    fileLines.add(scanner.nextLine());
                }
            }
            catch (FileNotFoundException e) {
                ErrorLogger.logError("Error reading file", e.toString(), false);
            }
        }
        
        return fileLines;
    }
    
    public static List<String> readFile(File inputFile) {
        List<String> fileLines = new ArrayList<>();
        if (inputFile.exists()) {
            try (Scanner scanner = new Scanner(inputFile)){
                while (scanner.hasNextLine()) {
                    fileLines.add(scanner.nextLine());
                }
            }
            catch (FileNotFoundException e) {
                ErrorLogger.logError("Error reading file", e.toString(), false);
            }
        }
        
        return fileLines;
    }
    
    public static void writeFile(String fileName, String input) {
        try (FileWriter fw = new FileWriter(fileName)){
            
            fw.write(input);
            fw.close();
        }
        catch (IOException e) {
            ErrorLogger.logError("Error writing to file", e.toString(), false);
        }
    }
    
    public static void writeFile(File inputFile, String input) {
        try (FileWriter fw = new FileWriter(inputFile)){
            
            fw.write(input);
            fw.close();
        }
        catch (IOException e) {
            ErrorLogger.logError("Error writing to file", e.toString(), false);
        }
    }
    
    public static void appendFile(String fileName, String input) {
        try (FileWriter fw = new FileWriter(fileName, true)){
            
            fw.write(input);
            fw.close();
        }
        catch (IOException e) {
            ErrorLogger.logError("Error writing to file", e.toString(), false);
        }
    }
    
    public static void deleteFile(String fileName) {
        File f = new File(fileName);
        try {
            Files.deleteIfExists(f.toPath());
        }
        catch (IOException e) {
            ErrorLogger.logError("Error Deleting file", e.toString(), false);
        }
    } 
}
