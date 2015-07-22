/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.NULogger;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
public class PogoPlugAccount extends AbstractAccount{
    private CookieStore cookieStore;
    private String responseString;
    
    private String uploadURL = "";
    public String validation_token = "";

    public PogoPlugAccount() {
        KEY_USERNAME = "pogoplgusername";
        KEY_PASSWORD = "pogoplgpassword";
        HOSTNAME = "PogoPlug.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();

            NULogger.getLogger().info("Trying to log in to PogoPlug.com");
            uploadURL = "http://service.pogoplug.com/svc/api/loginUser?email=" +getUsername()+ "&password=" +getPassword();
            responseString = NUHttpClientUtils.getData(uploadURL, httpContext);
            validation_token = StringUtils.stringBetweenTwoStrings(responseString, "\"valtoken\":\"", "\"");

            if (!validation_token.isEmpty()) {
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                NULogger.getLogger().info("PogoPlug.com login successful!");
            } else {
                //Generic exception
                throw new Exception("Login error: Invalid login credentials or the plugin might be broken");
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), e});
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }

    }

    private void initialize() throws Exception {
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
