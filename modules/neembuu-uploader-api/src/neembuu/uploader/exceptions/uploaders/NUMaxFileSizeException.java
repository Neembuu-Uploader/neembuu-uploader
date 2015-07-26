/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions.uploaders;

import java.util.logging.Level;
import java.util.logging.Logger;


import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.uploaders.common.GetSizeAsString;
//import neembuu.uploader.uploaders.common.CommonUploaderTasks;

/**
 * This handles all max file size exceptions within the classes of NU.
 * @author davidepastore
 */
public class NUMaxFileSizeException extends NUFileException {
    
    private String maxFileSize;
    
    /**
     * Constructs an instance of
     * <code>NUMaxFileSizeException</code> with the file name, max file size
     * and the hostname.
     * 
     * @param fileName the file name.
     * @param maxFileSize the max file size.
     * @param hostName the hostname.
     */
    public NUMaxFileSizeException(long maxFileSize, String fileName, String hostName) {
        super(NUException.MAX_FILE_SIZE);
        this.maxFileSize = GetSizeAsString.getSize(maxFileSize);
        this.fileName = fileName;
        this.hostName = hostName;
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        showMessageDialog( "<html><b>" + fileName+ ":<br/>"+Translation.T(this.getMessage())+" "+this.maxFileSize+"</html>", this.hostName);
    }
}
