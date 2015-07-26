/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions;

import java.util.logging.Level;
import java.util.logging.Logger;


import neembuu.uploader.translation.Translation;

/**
 * If the server has banned your IP.
 * @author davidepastore
 */
public class NUBannedIpException extends NUException {
    
    /**
     * Constructs an instance of
     * <code>NUBannedIpException</code> with the the host name.
     *
     * @param hostName the host name.
     */
    public NUBannedIpException(String hostName) {
        super(NUException.BANNED_IP, hostName);
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        showMessageDialog("<html><b>"+Translation.T(this.getMessage())+"</html>", this.hostName);
    }
}
