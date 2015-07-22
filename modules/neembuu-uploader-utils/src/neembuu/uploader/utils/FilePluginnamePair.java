package neembuu.uploader.utils;

import java.io.File;
import java.io.Serializable;
import neembuu.uploader.interfaces.Uploader;

/** Use this class instead of map.
 * If there was more than one rows with same file, then map will eliminate them.
 * So using this class.
 * 
 * This class is immutable and is marked as Serializable..
 * 
 * @author vigneshwaran
 */
public class FilePluginnamePair implements Serializable {
    //The file
    private final File file;
    //The class for that file.. Any class that implements Uploader interface.
    private final /*Class<? extends Uploader>*/String pluginName;

    /**
     * Pass in each file and its associated class
     * @param file
     * @param pluginName 
     */
    public FilePluginnamePair(File file, String pluginName) {
        this.file = file;
        this.pluginName = pluginName;
    }

    /**
     * 
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * 
     * @return the hostclass for that file
     */
    public String getHostPluginName() {
        return pluginName;
    }
    
    
}
