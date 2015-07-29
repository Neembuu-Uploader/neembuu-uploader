/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.interfaces;

import org.apache.http.protocol.HttpContext;

/**
 * This interface must be implemented if you are going to add an Account class.
 * @author vigneshwaran
 */
public interface Account {
    
    /**
     * 
     * @return username key for the particular account
     */
    public String getKeyUsername();
    
    /**
     * 
     * @return password key for the particular account
     */
    public String getKeyPassword();
    
    /**
     * 
     * @return Host name of the account class
     */
    public String getHOSTNAME();
    
    /**
     * 
     * @return the HttpContext for the account class
     */
    public HttpContext getHttpContext();
    
    /**
     * 
     * @return the username value
     */
    public String getUsername();
    
    /**
     * 
     * @return the current password
     */
    public String getPassword();
    
    /**
     * 
     * @return whether login was successful or not
     */
    public boolean isLoginSuccessful();
    
    /**
     * 
     * @return whether account is premium or not
     */
    public boolean isPremium();
    
    /**
     * Disables the account
     */
    public void disableLogin();
    
    /**
     * start the login process for the account
     */
    public void login();

    public boolean canLogin();
    
    /**
     * Check if the account is dead.
     * @return Returns true if the account is dead, false otherwise.
     */
    public boolean isDead();
    
    /**
     * Override default credentials for cli usage
     * @param username
     * @param password 
     */
    public void setOverridingCredentials(String username, String password);
}

