/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.utils;

/**
 *
 * @author Shashank
 */
public interface NUProperties {
    String getProperty(String key);
    String getEncryptedProperty(String key);
    
    void setProperty(String key, String value);
    void setEncryptedProperty(String key, String value);
}
