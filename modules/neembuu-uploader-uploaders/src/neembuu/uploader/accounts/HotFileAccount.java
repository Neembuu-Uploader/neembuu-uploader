/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author vigneshwaran
 * @author davidepastore
 */
public class HotFileAccount extends AbstractAccount {

    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpget;
    private CookieStore cookieStore;
    private String stringResponse;
    
    public static Cookie hfcookie = null;
    private long maxFileSizeLimit;

    public HotFileAccount() {
        KEY_USERNAME = "hfusername";
        KEY_PASSWORD = "hfpassword";
        HOSTNAME = "HotFile.com";
    }

    @Override
    public void login() {
        loginsuccessful = false;
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        
        //See if the user is premium or not
        try {
            this.getuserinfo();
        } catch(NUException ex){
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (IOException ex) {
            resetLogin();
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
            Logger.getLogger(HotFileAccount.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            NULogger.getLogger().info("Trying to log in to HotFile");
            
            httpPost = new NUHttpPost("http://www.hotfile.com/login.php");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("returnto", "%2F"));
            formparams.add(new BasicNameValuePair("user", getUsername()));
            formparams.add(new BasicNameValuePair("pass", getPassword()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            ////////////////////////////////////////////////////////////////////
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            
            if(CookieUtils.existCookie(httpContext, "auth")){
                hfcookie = CookieUtils.getCookie(httpContext, "auth");
                NULogger.getLogger().log(Level.INFO, "hotfile login successful auth:{0}", hfcookie.getValue());
                loginsuccessful = true;
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                username = getUsername();
                password = getPassword();
            }
            else{
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception ex) {
            resetLogin();
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
            Logger.getLogger(HotFileAccount.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Cookie getHfcookie() {
        return hfcookie;
    }
    

    @Override
    public void disableLogin() {
        loginsuccessful = false;
        //These code are necessary for account only sites.
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }
    
    /**
    * Get the max file size limit.
    * @return the max file size limit.
    */
    public long getMaxFileSizeLimit(){
        return this.maxFileSizeLimit;
    }
    
    /**
     * Implements <a href="http://api.hotfile.com/?c=getuserinfo">this function</a>.
     * @throws IOException 
     */
    private void getuserinfo() throws IOException, NUInvalidLoginException{
        String response;
        String isPremium;
        
        httpget = new NUHttpGet("http://api.hotfile.com/?action=getuserinfo&username="+getUsername()+"&password="+getPassword());
        httpResponse = httpclient.execute(httpget, httpContext);
        response = EntityUtils.toString(httpResponse.getEntity()).replace("\n", "").replace("\r", "");
        NULogger.getLogger().log(Level.INFO, "getuserinfo: {0}", response);
        if(response.contains("is_premium")){
            isPremium = StringUtils.stringBetweenTwoStrings(response, "=", "&");
            NULogger.getLogger().log(Level.INFO, "isPremium: {0}", isPremium);
            if("0".equals(isPremium)){
                maxFileSizeLimit  = 419430400l; //400 MB
            }
            if("1".equals(isPremium)){
                maxFileSizeLimit  = 2147483648l; //2 GB
            }
        }
        else{
            throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
        }
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
        hfcookie = null;
    }
}
