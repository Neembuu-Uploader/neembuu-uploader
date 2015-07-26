/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions.captcha;

import java.util.logging.Level;
import java.util.logging.Logger;


import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.translation.Translation;

/**
 * If the captcha is incorrect.
 * @author davidepastore
 */
public class NUCaptchaException extends NUException{
    
    /**
     * Constructs an instance of
     * <code>NUCaptchaException</code> with the host name.
     *
     * @param hostName the host name.
     */
    public NUCaptchaException(String hostName) {
        super(NUException.CAPTCHA_ERROR, hostName);
    }
    
    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        showMessageDialog( "<html><b>"+Translation.T(this.getMessage())+"</html>", this.hostName);
    }
    
}
