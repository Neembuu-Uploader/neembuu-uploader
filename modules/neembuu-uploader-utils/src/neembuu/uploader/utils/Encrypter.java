/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.utils;

import java.util.Random;

/**This class is uses string ciphering to encrypt the confidential details.. Don't change anything here..
 *
 * @author vigneshwaran
 */
public class Encrypter {

    //No need to create objects.
    private Encrypter() {
    }
    
    

    /**
     * Encrypts the value
     * @param value
     * @return 
     */
    public static String encrypt(String value){
        char[] c = value.toCharArray();
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        int count = 0;
        for(int i = 0; i<c.length;i++) {
            for(int j = 0; j<c.length-1;j++){
                sb.append((char)(r.nextInt(50)+70));
            }
            sb.append(c[i]);
        }
        return sb.toString();
    }
    
    /**
     * Decrypts the value.
     * @param value
     * @return 
     */
    public static String decrypt(String value){
        String s = value;
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        int noofchars = (int)Math.sqrt(value.length());
        char[] c = value.toCharArray();
        int index = noofchars;
        for(int i=0;i<noofchars;i++) {
            sb.append(c[index-1]);
            index += noofchars;
        }
        
        return sb.toString();
    }
}
