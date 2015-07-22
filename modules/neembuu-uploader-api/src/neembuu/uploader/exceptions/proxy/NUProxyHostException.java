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
 * This handles proxy host exception for NUHttpClient.
 * @author davidepastore
 */
public class NUProxyHostException extends NUException{
    
    private String proxyAddress;
    
    /**
     * Constructs an instance of
     * <code>NUProxyHostException</code> with the proxy address.
     *
     * @param proxyAddress the proxy address.
     */
    public NUProxyHostException(String proxyAddress) {
        super(NUException.INVALID_PROXY_HOST);
        this.proxyAddress = proxyAddress;
    }

    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(parent, "<html>"+Translation.T(this.getMessage())+" "+  this.proxyAddress + "</html>", Translation.T(this.getMessage()), JOptionPane.WARNING_MESSAGE);
    }
    
}
