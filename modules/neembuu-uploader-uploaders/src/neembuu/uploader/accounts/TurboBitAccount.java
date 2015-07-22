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
import neembuu.uploader.exceptions.accounts.NULockedAccountException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.Header;
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
import org.jsoup.nodes.Element;

/**
 *
 * @author davidepastore
 */
public class TurboBitAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    //Captcha
    private final String K_CHALLENGE_URL = "http://api.recaptcha.net/challenge?k=";
    private final String K_CHALLENGE_CODE = "6LcTGLoSAAAAAHCWY9TTIrQfjUlxu6kZlTYP50_c";

    public TurboBitAccount() {
        KEY_USERNAME = "trbbusername";
        KEY_PASSWORD = "trbbpassword";
        HOSTNAME = "TurboBit.net";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();

            NULogger.getLogger().info("Trying to log in to TurboBit.net");
            httpPost = new NUHttpPost("http://turbobit.net/user/login");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("user[login]", getUsername()));
            formparams.add(new BasicNameValuePair("user[pass]", getPassword()));
            formparams.add(new BasicNameValuePair("user[submit]", "Login"));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            Header lastHeader = httpResponse.getLastHeader("Location");
            
            // Captcha?
            if(lastHeader != null && lastHeader.getValue().equals("/login")){
                EntityUtils.consume(httpResponse.getEntity());
                responseString = NUHttpClientUtils.getData("http://turbobit.net/login", httpContext);
                //FileUtils.saveInFile("TurboBitAccount.html", responseString);
                Document doc = Jsoup.parse(responseString);
                String error = doc.select("form.login div.login_error").first().text();
                
                if(error.contains("Please enter the captcha.")){
                    NULogger.getLogger().info("Need to insert the captcha!");
                    
                    //Get the captcha code
                    Captcha captcha = captchaServiceProvider().newCaptcha();
                    captcha.setFormTitle(Translation.T().captchacontrol() + " (TurboBit.net)");
                    
                    if(captcha.findCCaptchaUrlFromK(K_CHALLENGE_URL + K_CHALLENGE_CODE) != null){
                        captcha.findCaptchaImageURL();
                        String captchaString = captcha.getCaptchaString();
                        
                        httpPost = new NUHttpPost("http://turbobit.net/user/login");
                        formparams = new ArrayList<NameValuePair>();
                        formparams.add(new BasicNameValuePair("user[login]", getUsername()));
                        formparams.add(new BasicNameValuePair("user[pass]", getPassword()));
                        formparams.add(new BasicNameValuePair("user[submit]", "Login"));
                        formparams.add(new BasicNameValuePair("user[captcha_subtype]", ""));
                        formparams.add(new BasicNameValuePair("user[captcha_type]", "recaptcha"));
                        formparams.add(new BasicNameValuePair("user[memory]", "on"));
                        formparams.add(new BasicNameValuePair("recaptcha_challenge_field", captcha.getCCaptchaUrl()));
                        formparams.add(new BasicNameValuePair("recaptcha_response_field", captchaString));
                        
                        entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                        httpPost.setEntity(entity);
                        httpResponse = httpclient.execute(httpPost, httpContext);
                        
                    }
                    else{
                        throw new Exception("Captcha generic error");
                    }
                }
                else if(error.contains("It has been temporarily locked")){
                    throw new NULockedAccountException(getUsername(), getHOSTNAME());
                }
                else{
                    throw new Exception("Other errror: " + error);
                }
            }
            
            //CookieUtils.printCookie(httpContext);
            
            if (CookieUtils.existCookie(httpContext, "user_isloggedin")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("TurboBit.net login successful!");

            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("TurboBitAccount.html", responseString);
                //Document doc = Jsoup.parse(responseString);
                //String error = doc.select("CSS path to error content").first().text();
                throw new Exception("Login error: captcha, username or password!");
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "TurboBit.net login exception: {0}", e);
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }

    }

    private void initialize() throws Exception {
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        //NULogger.getLogger().info("Getting startup cookies & link from TurboBit.net");
        //responseString = NUHttpClientUtils.getData("http://turbobit.net/", httpContext);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
 
