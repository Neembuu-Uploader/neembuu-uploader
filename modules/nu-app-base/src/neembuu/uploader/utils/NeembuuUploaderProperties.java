/* 
 * Copyright (C) 2015 Shashank Tulsyan <shashaank at neembuu.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package neembuu.uploader.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import neembuu.uploader.FindVersion;
//import neembuu.uploader.NeembuuUploader;
import neembuu.uploader.settings.Application;

/** This class is used to store the user specific properties like his username
 * and password on a .nuproperties file in the user's home folder.
 * 
 * There will be different .nuproperties file for different users whereas there
 * will be one settings.dat common for all users.
 *
 * @author vigneshwaran
 */
public class NeembuuUploaderProperties {

    //The property file unique to each user in their home folder
    public static final File propertyfile = Application.getNeembuuHome().resolve(".nuproperties").toFile();
    
    //Properties object that has all the values consistent with the properties file at any instant
    private static Properties properties = new Properties();
    
    //A comment
    public static String comment = "Neembuu Uploader Properties";

    /** Static Initializer - Create new file with default parameters 
     * if nothing exists */
    static {
        //If properties file doesn't already exist,, then
        if (!propertyfile.exists()) {
            try {
                //Create new properties file there..
                NULogger.getLogger().info("Creating new properties file");
                propertyfile.createNewFile();
                
                //Set Default properties
                setDefaultProperties();
            } catch (IOException ex) {
                NULogger.getLogger().log(Level.INFO, "Exception while creating property file: {0}", ex);
            }
        }
    }
    
    public static NUProperties getNUProperties(){
        return new NUProperties() {
            @Override public String getProperty(String key) {
                return NeembuuUploaderProperties.getProperty(key); }
            @Override public String getEncryptedProperty(String key) {
                return NeembuuUploaderProperties.getEncryptedProperty(key); }
            @Override public void setProperty(String key,String value) {
                NeembuuUploaderProperties.setProperty(key, value); }
            @Override public void setEncryptedProperty(String key, String value) {
                NeembuuUploaderProperties.setEncryptedProperty(key, value); }
        };
    }
    
    /**
     * Set up the properties object. It loads the properties from the file.
     * If the file doesn't exist, it will be created by the static initializer before call to this method executes.
     * If the file exists but in different version, then clear the properties and set the default properties again.
     * (no need to delete the file and create.. just clear the properties and set default)
     */
    public static void setUp() {
        
        NULogger.getLogger().info("Setting up properties..");
        
        
        //load the properties from file.
        loadProperties();
        
        //If version doesn't match the current one, then..
        if (!getProperty("version").equals(
                Float.toString(FindVersion.version())
                //NeembuuUploader.getVersionForProgam()
        )) {
            //Clear the properties object
            properties.clear();
            
            //Set default properties and overwrite the file with it.
            setDefaultProperties();
        }
    }

    /**
     * Sets default properties and overwrites the properties to the file.
     */
    private static void setDefaultProperties() {
        //Version will be needed
        setProperty("version", 
                Float.toString(FindVersion.version())
                //NeembuuUploader.getVersionForProgam()
        );
        
        //"firstlaunch" property will be needed to open the Accounts panel
        setProperty("firstlaunch", "true");
        
        //Store into file
        storeProperties();
    }

    /**
     * Write the properties to property file
     */
    public static void storeProperties() {
        NULogger.getLogger().fine("Storing Properties");
        try {
            properties.store(new FileOutputStream(propertyfile), comment);
        } catch (FileNotFoundException ex) {
            NULogger.getLogger().log(Level.INFO, "Properties file not found: {0}", ex);
        } catch (IOException ex) {
            NULogger.getLogger().log(Level.INFO, "IOException while writing property file {0}", ex);
        }
        NULogger.getLogger().fine("Properties stored successfully");
    }

    /**
     * Load the properties from property file
     */
    public static void loadProperties() {
        NULogger.getLogger().fine("Loading Properties");
        try {
            properties.load(new FileInputStream(propertyfile));
        } catch (FileNotFoundException ex) {
            NULogger.getLogger().log(Level.INFO, "Properties file not found: {0}", ex);
        } catch (IOException ex) {
            NULogger.getLogger().log(Level.INFO, "IOException while reading property file {0}", ex);
        }

        NULogger.getLogger().fine("Properties loaded successfully");

    }

    /**Sets the specified key and value
     * For passwords, use setEncryptedProperty
     * @param key
     * @param value 
     */
    public static void setProperty(String key, String value) {
        //Always remember to load the properties before storing
        loadProperties();
        properties.setProperty(key, value);
        storeProperties();
    }

    /**
     * Sets the specified key and the value.
     * Value will be encrypted before getting stored.
     * 
     * If you are not storing confidential data, use setProperty instead
     * 
     * @param key
     * @param value 
     */
    public static void setEncryptedProperty(String key, String value) {
        //Always remember to load the properties before storing
        loadProperties();
        
        //This condition will help save some unnecessary cycles.
        if(value.isEmpty()){
            properties.setProperty(key, value);
        }
        
        properties.setProperty(key, Encrypter.encrypt(value));
        storeProperties();
    }

    //Noninstantiable
    private NeembuuUploaderProperties() {
        // Don't create any objects
    }

    /**Get the value of a specified property
     * For passwords, call getEncryptedProperty
     * @param key The property key
     * @return The value of the given property if exists. If none exist, return ""
     */
    public static String getProperty(String key) {
        return properties.getProperty(key, "");
    }

    
    /**Get the value of a specified property. The stored value must have been encrypted.
     * So it'll be decrypted and returned.
     * 
     * For unencrypted values, use getProperty() instead.
     * 
     * @param key The property key
     * @return The value of the given property if exists, If none exist, return ""
     */
    public static String getEncryptedProperty(String key) {
        String value = properties.getProperty(key, "");
        if(value.isEmpty())
            return "";
        return Encrypter.decrypt(value);
    }

    
    /**Get the value of a specified property
     * For passwords, call getEncryptedProperty
     * @param key The property key
     * @param defaultValue A default value to return if none exists
     * @return The value of the given property if exists. If none exists, specified defaultValue will be returned.
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**If a value stored is a boolean, then use this method 
     * instead of using Boolean.valueOf(getProperty(key))
     * 
     * @param key The Key of the property
     * @return A boolean value that indicates whether the property has a true
     * value or not
     */
    public static boolean isPropertyTrue(String key) {
        return Boolean.valueOf(properties.getProperty(key, "false"));
    }

    /**
     * No need to use this unless there is some reason..
     * @return The property file
     */
    public static File getPropertyfile() {
        return propertyfile;
    }
}
