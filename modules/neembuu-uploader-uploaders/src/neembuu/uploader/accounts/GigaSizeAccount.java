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
public class GigaSizeAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;

    private static String formtoken;
    private static StringBuilder gigasizecookies = null;

    public GigaSizeAccount() {
        KEY_USERNAME = "gsusername";
        KEY_PASSWORD = "gspassword";
        HOSTNAME = "GigaSize.com";
    }

    public static StringBuilder getGigasizecookies() {
        return gigasizecookies;
    }

    @Override
    public void disableLogin() {
        loginsuccessful = false;
        if (gigasizecookies != null) {
            gigasizecookies.setLength(0);
        }
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            initialize();

            NULogger.getLogger().info("Trying to log in to Giga Size");
            httpPost = new NUHttpPost("http://www.gigasize.com/signin");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("func", ""));
            formparams.add(new BasicNameValuePair("token", formtoken));
            formparams.add(new BasicNameValuePair("signRem", "1"));
            formparams.add(new BasicNameValuePair("email", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());

            NULogger.getLogger().info("Getting cookies........");
            gigasizecookies = new StringBuilder(CookieUtils.getAllCookies(httpContext));
            
            NULogger.getLogger().info(gigasizecookies.toString());

            if (CookieUtils.existCookie(httpContext, "MIIS_GIGASIZE_AUTH")) {
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("GigaSize Login Success");
            } else {
                //Handle errors
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
            Logger.getLogger(FileFactoryAccount.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initialize() throws IOException {
        gigasizecookies = new StringBuilder();
        NULogger.getLogger().info("Getting startup cookies from gigasize.com");
        
        httpGet = new NUHttpGet("http://www.gigasize.com/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        gigasizecookies = new StringBuilder(CookieUtils.getAllCookies(httpContext));
        formtoken = getData("http://www.gigasize.com/formtoken");
        NULogger.getLogger().log(Level.INFO, "formtoken: {0}", formtoken);
    }

    public String getData(String url) {
        try {
            httpGet = new NUHttpGet(url);
            httpResponse = httpclient.execute(httpGet, httpContext);
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            NULogger.getLogger().log(Level.INFO, "exception : {0}", e.toString());
            return "";
        }
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
        gigasizecookies = null;
        formtoken = "";
    }
}
