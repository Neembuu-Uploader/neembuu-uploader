/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions.uploaders;

import java.util.logging.Level;
import java.util.logging.Logger;


import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.translation.Translation;

/**
 * If the upload failed for upload limit exceeded.
 * I think it refers to upload speed.
 * Take a look <a href="http://crocko.com/it/developers.html">here</a>.
 * @author davidepastore
 */
public class NUUploadLimitExceededException extends NUFileException {
    
    /**
     * Constructs an instance of
     * <code>NUUploadLimitExceededException</code> with the file name
     * and the hostname.
     * 
     * @param fileName the file name.
     * @param hostName the hostname.
     */
    public NUUploadLimitExceededException(String fileName, String hostName) {
        super(NUException.UPLOAD_LIMIT_EXCEEDED);
        this.fileName = fileName;
        this.hostName = hostName;
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        showMessageDialog( "<html><b>" +fileName+ ":<br/>"+Translation.T(this.getMessage())+"</html>", this.hostName);
    }
}
