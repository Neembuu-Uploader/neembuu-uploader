/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions.accounts;

import java.util.logging.Level;
import java.util.logging.Logger;


import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.translation.Translation;

/**
 * Account not actived exception: user needs to activate his account.
 * @author davidepastore
 */
public class NUAccountNotActivedException extends NUAccountException{
    
    /**
     * Constructs an instance of
     * <code>NUAccountNotActivedException</code> with the username and the hostname.
     *
     * @param userName the username.
     * @param hostName the hostname.
     */
    public NUAccountNotActivedException(String userName, String hostName) {
        super(NUException.ACCOUNT_NOT_ACTIVED);
        this.hostName = hostName;
        this.userName = userName;
    }
    
    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        showMessageDialog( "<html><b>" + userName+ ":<br/>"+Translation.T(this.getMessage())+"</html>", this.hostName);
    }
}
