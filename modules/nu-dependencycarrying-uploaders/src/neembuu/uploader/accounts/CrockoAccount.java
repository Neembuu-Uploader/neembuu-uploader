/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.uploaders.api._crocko.CrockoApi;
import neembuu.uploader.utils.NULogger;

/**
 *
 * @author dinesh
 * @author davidepastore
 */
public class CrockoAccount extends AbstractAccount {

    private String apikey;

    public CrockoAccount() {
        KEY_USERNAME = "crusername";
        KEY_PASSWORD = "crpassword";
        HOSTNAME = "Crocko.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    
    @Override
    public void login() {

        loginsuccessful = false;
        try{
            apikey = CrockoApi.getAPIkey(getUsername(), getPassword());
            if(apikey != null){
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                
                NULogger.getLogger().log(Level.INFO, "Crocko api key is: {0}", apikey);
                NULogger.getLogger().info("Crocko login successful :)");
            }
            else{
                throw new NUInvalidLoginException(getUsername(), HOSTNAME);
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
            
        } catch (Exception e){
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), e.toString()});
            resetLogin();
            
            NULogger.getLogger().info("Crocko.com Login failed :(");
                
            showWarningMessage(Translation.T().loginerror() +"\n" + CrockoApi.getError()+":"+CrockoApi.getErrorMessage(), HOSTNAME);
            accountUIShow().setVisible(true);
            }
        }
    
    /**
     * Return the apikey of this account.
     * @return The apikey. 
     */
    public String getAPIkey() {
        return apikey;
    }
    
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
