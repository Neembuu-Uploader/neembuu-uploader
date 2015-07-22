/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions.uploaders;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.translation.Translation;

/**
 * If the upload failed for a generic reason.
 * @author davidepastore
 */
public class NUUploadFailedException extends NUFileException {
    
    /**
     * Constructs an instance of
     * <code>NUUploadFailedException</code> with the file name
     * and the hostname.
     * 
     * @param fileName the file name.
     * @param hostName the hostname.
     */
    public NUUploadFailedException(String fileName, String hostName) {
        super(NUException.UPLOAD_FAILED);
        this.fileName = fileName;
        this.hostName = hostName;
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(parent, "<html><b>" + fileName+ ":<br/>"+Translation.T(this.getMessage())+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
}
