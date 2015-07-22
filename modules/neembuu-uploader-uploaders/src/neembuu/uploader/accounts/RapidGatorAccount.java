/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author vigneshwaran
 * @author davidepastore
 */
public class RapidGatorAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private StringBuilder loginCookie = null;

    public RapidGatorAccount() {
        KEY_USERNAME = "rgusername";
        KEY_PASSWORD = "rgpassword";
        HOSTNAME = "RapidGator.net";
    }

    @Override
    public void disableLogin() {

        loginsuccessful = false; 
        if (loginCookie != null) {
            loginCookie.setLength(0);
        }
        
        resetLogin();
        
        //These code are necessary for account only sites.
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {

            if (loginCookie == null) {
                loginCookie = new StringBuilder();
            }
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            NULogger.getLogger().info("Trying to log in to rapidgator.net");

            httpPost = new NUHttpPost("https://rapidgator.net/auth/login");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("LoginForm[email]:", getUsername()));
            formparams.add(new BasicNameValuePair("LoginForm[password]:", getPassword()));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            loginCookie = new StringBuilder(CookieUtils.getAllCookies(httpContext));

            /*
            Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
            Cookie escookie = null;
            while (it.hasNext()) {
                escookie = it.next();
                loginCookie.append(escookie.getName()).append("=").append(escookie.getValue()).append(";");
//                NULogger.getLogger().info(escookie.getName() + " : " + escookie.getValue());
            }
            */

            if (CookieUtils.existCookie(httpContext, "user")) {
                loginsuccessful = true;
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("RapidGator.net login succeeded :)");

            } else {
                NULogger.getLogger().info("RapidGator.net Login failed :(");
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }
        } catch(NUException ex){
            //If login failed, then showing error message and showing accounts manager again
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);

        } catch (Exception e) {
            Logger.getLogger(RapidGatorAccount.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * Reset login.
     */
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
    
    /**
     * Get login cookie string.
     * @return Returns login cookie string.
     */
    public String getLoginCookie() {
        return loginCookie.toString();
    }
}
