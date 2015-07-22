/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.zip.generator;

import java.io.File;
import org.apache.commons.io.FilenameUtils;

/**
 * Represents a plugin to update.
 * @author davidepastore
 */
public class PluginToUpdate {
    
    private File file;
    private String sha;
    
    /**
     * Create a new instance of PluginToUpdate.
     * @param file The file that you want to update.
     * @param sha The sha of the given file.
     */
    public PluginToUpdate(File file, String sha){
        this.file = file;
        this.sha = sha;
    }
    
    /**
     * Get the name of the plugin.
     * It is recovered from the file name.
     * @return Returns the name of the plugin.
     */
    public String getName(){
        return FilenameUtils.removeExtension(file.getName());
    }
    
    /**
     * Get the sha.
     * @return Returns the sha.
     */
    public String getSha(){
        return sha;
    }
    
}
