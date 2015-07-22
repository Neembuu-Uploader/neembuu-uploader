/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
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
 * @author dinesh
 */
public class NetLoadAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;

    private static String phpsessioncookie;
    private static String usercookie;

    public NetLoadAccount() {
        KEY_USERNAME = "nlusername";
        KEY_PASSWORD = "nlpassword";
        HOSTNAME = "Netload.in";
    }
    
    

    public String getPhpsessioncookie() {
        return phpsessioncookie;
    }

    public String getUsercookie() {
        return usercookie;
    }


    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    private void initialize() throws Exception {
        httpGet = new NUHttpGet("http://netload.in/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        phpsessioncookie = CookieUtils.getCookieNameValue(httpContext, "PHPSESSID");
        
        NULogger.getLogger().log(Level.INFO, "PHP session cookie : {0}", phpsessioncookie);
    }


    @Override
    public void login() {
        loginsuccessful = false;
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        try {
            initialize();
            
            NULogger.getLogger().info("Trying to log in to netload.in");
            httpPost = new NUHttpPost("http://netload.in/index.php");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("txtuser", getUsername()));
            formparams.add(new BasicNameValuePair("txtpass", getPassword()));
            formparams.add(new BasicNameValuePair("txtcheck", "login"));
            formparams.add(new BasicNameValuePair("txtlogin", "Login"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            
            NULogger.getLogger().info("Getting cookies........");
            if(CookieUtils.existCookie(httpContext, "cookie_user")){
                usercookie = CookieUtils.getCookieNameValue(httpContext, "cookie_user");
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info(usercookie);
                NULogger.getLogger().info("Netload login successful :)");
            }
            else{
                //Handle errors
                //FileUtils.saveInFile("NetLoadAccount.html", stringResponse);
                
                //Generic error
                throw new Exception("Error in Mediafire login.");
            }

        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "NetLoad Login error: {0}", e);
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }
    
    
    private void resetLogin(){
        loginsuccessful = false;
        phpsessioncookie = "";
        usercookie = "";
        username = "";
        password = "";
    }
}
