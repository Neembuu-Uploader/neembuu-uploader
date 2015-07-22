/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.captcha.Captcha;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.exceptions.captcha.NUCaptchaException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
public class KeepTwoShareAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    //Captcha
    private boolean activatedCaptcha = false;
    private String captchaAddress = "";
//    private final String kChallengeURL = "http://www.google.com/recaptcha/api/challenge?k=";
//    private final String kChallengeCode = "6LcYcN0SAAAAABtMlxKj7X0hRxOY8_2U86kI1vbb";

    public KeepTwoShareAccount() {
        KEY_USERNAME = "kptsusername";
        KEY_PASSWORD = "kptspassword";
        HOSTNAME = "Keep2Share.cc";
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

            NULogger.getLogger().info("Trying to log in to Keep2Share.cc");
            httpPost = new NUHttpPost("http://keep2share.cc/login.html");
            
            Document doc = Jsoup.parse(NUHttpClientUtils.getData("http://keep2share.cc/login.html", httpContext));
            
            if(doc.getElementById("captcha_auth0") != null){
                activatedCaptcha = true;
                captchaAddress = "http://keep2share.cc" + doc.getElementById("captcha_auth0").attr("src");
            }
            
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            
            formparams.add(new BasicNameValuePair("LoginForm[password]", getPassword()));
            formparams.add(new BasicNameValuePair("LoginForm[rememberMe]", "0"));
            formparams.add(new BasicNameValuePair("LoginForm[username]", getUsername()));
            
            if(activatedCaptcha){
                Captcha captcha = captchaServiceProvider().newCaptcha();
                captcha.setFormTitle(Translation.T().captchacontrol()+" ("+ getHOSTNAME()  +")");
                captcha.setImageURL(captchaAddress);
                captcha.setHttpContext(httpContext);
                String captchaString = captcha.getCaptchaString();

                formparams.add(new BasicNameValuePair("LoginForm[verifyCode]", captchaString));
//                formparams.add(new BasicNameValuePair("recaptcha_challenge_field", captcha.getCCaptchaUrl()));
//                formparams.add(new BasicNameValuePair("recaptcha_response_field", captchaString));
                formparams.add(new BasicNameValuePair("yt0", ""));
            }
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            if(responseString.contains("Incorrect username or password")){
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }
            
            if(responseString.contains("The verification code is incorrect.")){
                throw new NUCaptchaException(getHOSTNAME());
            }
            
            if(CookieUtils.existCookie(httpContext, "sessid")){
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                NULogger.getLogger().info("Keep2Share.cc login successful!");
            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("KeepTwoShareAccount.html", responseString);

                //Generic exception
                throw new Exception("Generic login error");
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

        //NULogger.getLogger().info("Getting startup cookies & link from Keep2Share.cc");
        //responseString = NUHttpClientUtils.getData("http://keep2share.cc/", httpContext);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
 
