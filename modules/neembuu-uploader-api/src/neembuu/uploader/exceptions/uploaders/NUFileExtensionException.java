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
 * If the file that you want to upload has a not allowed extension.
 * @author davidepastore
 */
public class NUFileExtensionException extends NUFileException {
    
    /**
     * Constructs an instance of
     * <code>NUUploadFailedException</code> with the file name
     * and the hostname.
     * @param fileName the file name.
     * @param hostName the hostname.
     */
    public NUFileExtensionException(String fileName, String hostName) {
        super(NUException.FILE_EXTENSION);
        this.fileName = fileName;
        this.hostName = hostName;
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(parent, "<html><b>" + fileName+ ":<br/>"+Translation.T().filetypenotsupported()+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
}
