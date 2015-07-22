/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions.proxy;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.translation.Translation;

/**
 * This handles proxy port exception for NUHttpClient.
 * @author davidepastore
 */
public class NUProxyPortException extends NUException{
    
    private String proxyPort;
    
    /**
     * Constructs an instance of
     * <code>NUProxyPortException</code> with the proxy port.
     *
     * @param proxyPort the proxy port.
     */
    public NUProxyPortException(String proxyPort) {
        super(NUException.INVALID_PROXY_PORT);
        this.proxyPort = proxyPort;
    }

    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(parent, "<html>"+Translation.T(this.getMessage())+" "+ this.proxyPort + "</html>", Translation.T(this.getMessage()), JOptionPane.WARNING_MESSAGE);
    }
}
