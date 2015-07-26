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
 * If the file that you want to upload is in black list.
 * @author davidepastore
 */
public class NUFileInBlackListException extends NUFileException {
    
    /**
     * Constructs an instance of
     * <code>NUFileInBlackListException</code> with the file name and the host name.
     *
     * @param fileName the file name.
     * @param hostName the host name.
     */
    public NUFileInBlackListException(String fileName, String hostName) {
        super(NUException.FILE_IN_BLACK_LIST);
        this.fileName = fileName;
        this.hostName = hostName;
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        showMessageDialog( "<html><b>" + fileName+ ":<br/>"+Translation.T(this.getMessage())+"</html>", this.hostName);

    }
}
