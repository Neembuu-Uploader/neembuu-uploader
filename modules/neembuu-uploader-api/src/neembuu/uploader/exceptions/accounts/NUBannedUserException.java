/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions.accounts;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.translation.Translation;

/**
 * Banned user exception: user is banned.
 * @author davidepastore
 */
public class NUBannedUserException extends NUAccountException{
    
    /**
     * Constructs an instance of
     * <code>NUBannedUserException</code> with the username and the hostname.
     *
     * @param userName the username.
     * @param hostName the hostname.
     */
    public NUBannedUserException(String userName, String hostName) {
        super(NUException.BANNED_USER);
        this.hostName = hostName;
        this.userName = userName;
    }
    
    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(parent, "<html><b>" + userName+ ":<br/>"+Translation.T(this.getMessage())+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
}
