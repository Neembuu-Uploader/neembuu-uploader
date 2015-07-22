/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import neembuu.uploader.translation.Translation;

/**
 * If the server declined to allow the requested access.
 * @author davidepastore
 */
public class NUHTTP403Exception extends NUException {
    
    /**
     * Constructs an instance of
     * <code>NUHTTP403Exception</code> with the host name.
     *
     * @param hostName the host name.
     */
    public NUHTTP403Exception(String hostName) {
        super(NUException.HTTP_403, hostName);
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(parent, "<html><b>"+Translation.T(this.getMessage())+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
}
