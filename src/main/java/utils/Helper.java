/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class used to implement helper functions
 * @author michael
 */
public final class Helper {
    
    
    public static final String getTimestamp() {
        return getTimestamp(0);
    }
    
    /**
     * Get the current timestamp as a string. <br>
     * Timestamp is formatted "yyyy-MM-dd HH:mm:ss"
     * @return current timestamp
     */
    public static final String getTimestamp(int offsetDays) {
        Instant instant = Instant.now();
        
        if (offsetDays > 0) {
            instant = instant.plus(offsetDays, ChronoUnit.DAYS);
        }

        DateTimeFormatter formatDate = DateTimeFormatter
            .ofPattern(
                "yyyy-MM-dd HH:mm:ss"
            ).withZone(
                ZoneId.systemDefault()
            );

        String timestamp = formatDate.format(instant);
        
        return timestamp;
    }
    
    /**
     * Remove characters from start of string
     * @param string string to be modified
     * @param chars array of characters to be removed
     * @return modified string
     */
    public static final  String stripLeadingChars(String string, Character[] chars) {
        try {
            while (inArray((Character) string.charAt(0),chars)) {
                if (string.length() == 1) {return "";}
                string = string.substring(1);
            }
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
        return string;
    }
    
    public static final  String stripLeadingChars(String string, char c) {
        Character[] chars = {c};
    return stripLeadingChars(string, chars);
    }
    
    /**
     * Check if an object exists in an array of objects. Does not accept primitive types.
     * @param <T>
     * @param item
     * @param array
     * @return true/false
     */
    public static final <T> boolean inArray(T item, T[] array) {
        for (int i = 0; i < array.length; i++) {
            if (item.equals(array[i])) {
                return true;
            }
        }
        return false;
    }
    
    
    public static final String summarise(String fullText, int maxLength, boolean ellipsis) {
        if (fullText.length() > maxLength) {
            return fullText.substring(0, maxLength) + (ellipsis? "..." : "");
        }
        return fullText;
    }
    
}
