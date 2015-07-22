/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.utils;

/**
 * Integer utils.
 * @author davidepastore
 */
public class IntegerUtils {
    
    /**
     * Check if a string is or not an Integer value.
     * @param s the string
     * @return Boolean true or false.
     */
    public static boolean isInteger(String s) {
        try { 
            Integer.parseInt(s); 
        } catch(NumberFormatException e) { 
            return false; 
        }
        // only got here if we didn't return false
        return true;
    }
    
}
