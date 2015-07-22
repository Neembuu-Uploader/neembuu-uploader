/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders.common;



/**
 * This class provides simple common methods to work with Strings.
 * @author davidepastore
 */
public class StringUtils{
    
    /**
     * Non-instantiable
     */
    private StringUtils() {
    }

    /**
     *
     * @param response
     * @param stringStart
     * @param stringEnd
     * @return Return the content of response string between stringStart and stringEnd.
     */
    public static String stringBetweenTwoStrings(String response, String stringStart, String stringEnd) {
        return stringBetweenTwoStrings(response, stringStart, stringEnd, false);
    }

    /**
     *
     * @param response
     * @param stringStart
     * @param stringEnd
     * @param lastindexof
     * @return Return the content of response string between stringStart and stringEnd. If lastindexof is set,
     * the string is evaluated from lastIndexOf stringStart.
     */
    public static String stringBetweenTwoStrings(String response, String stringStart, String stringEnd, boolean lastindexof) {
        if (!lastindexof) {
            response = stringStartingFromString(response, stringStart);
        } else {
            response = response.substring(response.lastIndexOf(stringStart));
            response = response.replaceFirst(stringStart, "");
        }
        response = stringUntilString(response, stringEnd);
        return response;
    }

    /**
     * Create the string until <i>stringEnd</i>.
     *
     * @param string
     * @param stringEnd
     * @return Return the string until <i>stringEnd</i>.
     */
    public static String stringUntilString(String string, String stringEnd) {
        return string.substring(0, string.indexOf(stringEnd));
    } 
    
    /**
     * Create the string starting from <i>stringStart</i>.
     * @param string
     * @param stringStart
     * @return Return the string starting from <i>stringStart</i>.
     */
    public static String stringStartingFromString(String string, String stringStart){
        string = string.substring(string.indexOf(stringStart));
        return string.replaceFirst(stringStart, "");
    }
    
    /**
     * Create the string starting from <i>stringStart</i>.
     * @param string
     * @param stringStart
     * @param regExp
     * @return Return the string starting from <i>stringStart</i>.
     */
    public static String stringStartingFromString(String string, String stringStart, boolean regExp){
        string = string.substring(string.indexOf(stringStart));
        if(regExp){
            return string.replaceFirst(stringStart, "");
        }
        else{
            return string.replace(stringStart, "");
        }
    }
    
    /**
     * Calculate an uuid with size random numbers.
     * @param size the size of the uuid
     * @param number the number to multiply with Math.random()
     * @return The calculated uuid.
     */
    public static String uuid(int size, int number) {
        String uid = "";
        for (int i = 0; i < size; i++) {
            uid += (int) (Math.random() * number);
        }
        //NULogger.getLogger().info(uid);
        return uid;
    }
    
    
    /**
     * Remove the first char of a string.
     * @param str the String object.
     * @return The string without the first char.
     */
    public static String removeFirstChar(String str) {
        return removeFirstChars(str, 1);
    }
    
    
    /**
     * Remove the first char of a string.
     * @param str the String object.
     * @param number the number of chars to delete.
     * @return The string without the number first char.
     */
    public static String removeFirstChars(String str, int number) {
        return str.substring(number);
    }
    
    
    /**
     * Remove the last char of a string.
     * @param str the String object.
     * @return The string without the last char.
     */
    public static String removeLastChar(String str) {
        return removeLastChars(str, 1);
    }
    
    
    /**
     * Remove the last n chars of a string.
     * @param str the String object.
     * @param number the number of chars to delete.
     * @return The string without the number last n chars.
     */
    public static String removeLastChars(String str, int number) {
        return str.substring(0, str.length() - number);
    }
    
    
    /**
     * Get the size in bytes from a String like "30 KB", "73 MB", etc...
     * @param stringSize the string with the size.
     * @return Returns a long with the size in bytes.
     */
    public static long getSizeFromString(String stringSize) {
        long multiplier = 1;
        String result = "";
        if (stringSize.contains("KB")) {
            multiplier = 1024;
        } else if (stringSize.contains("MB")) {
            multiplier = 1048576;
        } else if (stringSize.contains("GB")) {
            multiplier = 1073741824L;
        }
        for (int i = 0; i < stringSize.length(); i++) {
            char character = stringSize.charAt(i);
            if (Character.isDigit(character)) {
                result += character;
            }
        }
        return Integer.parseInt(result) * multiplier;
    }
    
}
