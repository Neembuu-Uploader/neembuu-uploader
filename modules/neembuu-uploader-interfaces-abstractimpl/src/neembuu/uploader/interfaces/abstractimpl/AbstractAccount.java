/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.interfaces.abstractimpl;

import neembuu.release1.api.ui.MainComponent;
import neembuu.uploader.api.accounts.AccountSelectionUI;
import neembuu.uploader.api.accounts.HostsAccountUI;
import neembuu.uploader.api.accounts.UpdateSelectedHostsCallback;
import neembuu.uploader.captcha.CaptchaServiceProvider;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.translation.ToHtmlMultiLine;
import neembuu.uploader.utils.NUProperties;
//import neembuu.uploader.utils.NeembuuUploaderProperties;
import org.apache.http.protocol.HttpContext;

/**
 *
 * @author vigneshwaran
 */
public abstract class AbstractAccount implements Account {

    //These 3 variables must be overridden specifically by the plugin developer.
    protected String KEY_USERNAME = "";
    protected String KEY_PASSWORD = "";
    protected String HOSTNAME = "";
    
    //This account is premium?
    protected boolean premium = false;
    
    /**
     * Is this account dead?
     */
    protected boolean isDead = false;
    
    protected HttpContext httpContext;
    
    public String username = "";
    public String password = "";
    public boolean loginsuccessful = false;
    
    private static NUProperties properties = null;
    private static MainComponent mainComponent = null;
    private static AccountSelectionUI accountUIShow = null;
    private static UpdateSelectedHostsCallback updateSelectedHostsCallback;
    private static HostsAccountUI hostsAccountUI;
    private static CaptchaServiceProvider captchaServiceProvider;
    
    public static void init(NUProperties properties,MainComponent mainComponent,
            AccountSelectionUI uIShow,UpdateSelectedHostsCallback updateSelectedHostsCallback,
            HostsAccountUI hostsAccountUI,CaptchaServiceProvider captchaServiceProvider){
        if(AbstractAccount.properties!=null){
            throw new IllegalStateException("Alreayd initialized");
        }
        AbstractAccount.properties = properties;
        AbstractAccount.mainComponent = mainComponent;
        AbstractAccount.accountUIShow = uIShow;
        AbstractAccount.updateSelectedHostsCallback = updateSelectedHostsCallback;
        AbstractAccount.hostsAccountUI = hostsAccountUI;
        AbstractAccount.captchaServiceProvider = captchaServiceProvider;
    }
    
    protected static CaptchaServiceProvider captchaServiceProvider(){
        return captchaServiceProvider;
    }
    
    protected static HostsAccountUI hostsAccountUI(){
        return hostsAccountUI;
    }
    
    protected static AccountSelectionUI accountUIShow(){
        return accountUIShow;
    }
    
    protected static NUProperties properties(){
        return properties;
    }

    protected static void showWarningMessage(String message,String title){
        message = ToHtmlMultiLine.splitToMultipleLines(message, 50);
        mainComponent.newMessage().warning()
                .setTitle(title)
                .setMessage(message)
                .setTimeout(10000)
                .show();
    }
    
    protected static void updateSelectedHostsLabel(){
        updateSelectedHostsCallback.updateSelectedHostsLabel();
    }

    public String getKeyUsername() {
        return KEY_USERNAME;
    }

    public String getKeyPassword() {
        return KEY_PASSWORD;
    }

    public String getHOSTNAME() {
        return HOSTNAME;
    }
    
    public HttpContext getHttpContext() {
        return httpContext;
    }

    public String getUsername() {
        return properties.getProperty(KEY_USERNAME);
        //return NeembuuUploaderProperties.getProperty(KEY_USERNAME);
    }

    public String getPassword() {
        return properties.getEncryptedProperty(KEY_PASSWORD);
        //return NeembuuUploaderProperties.getEncryptedProperty(KEY_PASSWORD);
    }

    public boolean isLoginSuccessful() {
        return loginsuccessful;
    }
    
    public boolean isPremium() {
        return premium;
    }
    
    /**This is to prevent logging in when the credentials are changed */
    public boolean canLogin() {
        if(username.equals(getUsername()) && password.equals(getPassword()))
            return false;
        return true;
    }

    public abstract void disableLogin();

    public abstract void login();
    
    /**
     * Check if the account is dead.
     * @return Returns true if the account is dead, false otherwise.
     */
    public boolean isDead(){
        return isDead;
    }
    
    @Override
    public void setOverridingCredentials(String username, String password){
        this.properties.setProperty(KEY_USERNAME, username);
        this.properties.setEncryptedProperty(KEY_PASSWORD, password);
    }
   
}
