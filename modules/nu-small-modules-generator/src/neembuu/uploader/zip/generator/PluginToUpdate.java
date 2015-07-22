/* 
 * Copyright 2015 Shashank Tulsyan <shashaank at neembuu.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
