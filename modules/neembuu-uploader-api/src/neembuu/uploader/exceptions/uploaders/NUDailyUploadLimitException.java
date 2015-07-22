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
 * This handles all daily upload limit exceptions within the classes of NU.
 * @author davidepastore
 */
public class NUDailyUploadLimitException extends NUFileException {
    
    private int dailyUploadLimit;
    
    /**
     * Constructs an instance of
     * <code>NUDailyUploadLimitException</code> with
     * the maximum number of daily upload, the file name and the host name.
     *
     * @param dailyUploadLimit the number of file you can upload.
     * @param fileName the file name.
     * @param hostName the hostname.
     */
    public NUDailyUploadLimitException(int dailyUploadLimit, String fileName, String hostName) {
        super(NUException.DAILY_UPLOAD_LIMIT);
        this.dailyUploadLimit = dailyUploadLimit;
        this.fileName = fileName;
        this.hostName = hostName;
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(parent, "<html><b>" + fileName+ ":<br/>"+Translation.T(this.getMessage())+" "+this.dailyUploadLimit+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
}
