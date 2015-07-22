/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
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
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.captcha.Captcha;
import neembuu.uploader.translation.Translation;

/**
 *
 * @author vigneshwaran
 * @author davidepastore
 */
public class FileCloudAccount extends AbstractAccount {
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private String message;
    private String captchaString;

    private final String kChallengeURL = "http://www.google.com/recaptcha/api/challenge?k=";
    private final String kChallengeCode = "6Lf5OdISAAAAAEZObLcx5Wlv4daMaASRov1ysDB1";

    public FileCloudAccount() {
        KEY_USERNAME = "fcusername";
        KEY_PASSWORD = "fcpassword";
        HOSTNAME = "FileCloud.io";
        
        setupSsl();
    }

    @Override
    public void disableLogin() {
        resetLogin();
        //These code are necessary for account only sites.
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    volatile Captcha captcha = null;
    
    @Override
    public void login() {
        loginsuccessful = false;
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            
            NULogger.getLogger().info("Getting startup cookies & link from FileCloud.io");
            stringResponse = NUHttpClientUtils.getData("https://filecloud.io/user-login.html", httpContext);
            Captcha captchaNew,captchaPrevious;
            synchronized (this){
                captchaPrevious = captcha;
                captchaNew = captchaServiceProvider().newCaptcha();
                captcha = captchaNew;
            }
            if(captchaPrevious!=null){
                try{captchaPrevious.destroy();}catch(Exception a){a.printStackTrace();}
            }
            captchaNew.setFormTitle(Translation.T().captchacontrol()+" (FileCloud.io)");
            if(captchaNew.findCCaptchaUrlFromK(kChallengeURL+kChallengeCode) != null){
                captchaNew.findCaptchaImageURL();
                new Exception().printStackTrace();
                captchaString = captchaNew.getCaptchaString();
                
                httpPost = new NUHttpPost("https://filecloud.io/user-login_p.html");
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("username", getUsername()));
                formparams.add(new BasicNameValuePair("password", getPassword()));
                formparams.add(new BasicNameValuePair("recaptcha_challenge_field", captchaNew.getCCaptchaUrl()));
                formparams.add(new BasicNameValuePair("recaptcha_response_field", captchaString));
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                httpPost.setEntity(entity);
                httpResponse = httpclient.execute(httpPost, httpContext);
                stringResponse = EntityUtils.toString(httpResponse.getEntity());
            }
            
            if(stringResponse.contains("you have successfully logged in") && !CookieUtils.getCookieValue(httpContext, "auth").isEmpty()){
                loginsuccessful = true;
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("FileCloud Login success :)");
            }
            
            if("error".equals(stringResponse)){
                //NULogger.getLogger().log(Level.INFO, "Message: {0}", message);
                
                if("no such user or wrong password".equals(message)){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
                
                //Handle other errors
                throw new Exception("Login doesn't work: " + message);
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            showWarningMessage( Translation.T().loginerror(), HOSTNAME); 
            accountUIShow().setVisible(true);
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "{0}: Error in FileCloud Login \n Exception: {1}", new Object[]{getClass().getName(), e});
        }
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
    
    private void setupSsl() {
        SSLSocketFactory sf = null;
        SSLContext sslContext = null;

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (NoSuchAlgorithmException e) {
            NULogger.getLogger().log(Level.SEVERE, "FileCloud.io -> SSL error", e);
        } catch (KeyManagementException e) {
            NULogger.getLogger().log(Level.SEVERE, "FileCloud.io -> SSL error", e);
        }

        try {
            sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            
        } catch (Exception e) {
            NULogger.getLogger().log(Level.SEVERE, "FileCloud.io -> SSL error", e);
        }

        Scheme scheme = new Scheme("https", 443, sf);
        httpclient.getConnectionManager().getSchemeRegistry().register(scheme);
    }
}
