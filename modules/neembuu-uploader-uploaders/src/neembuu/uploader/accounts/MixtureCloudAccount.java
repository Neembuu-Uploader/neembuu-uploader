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
public class MixtureCloudAccount extends AbstractAccount{
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String secureCode;
    
    //Captcha
    private String kChallengeURL = "https://www.google.com/recaptcha/api/challenge?k=";
    private String kChallengeCode = "6Ld7c9ISAAAAADeAGevtJ0zpz4nfVmjUNt5-Caqh";
    private String recaptchaChallengeField;

    public MixtureCloudAccount() {
        KEY_USERNAME = "mcbusername";
        KEY_PASSWORD = "mcbpassword";
        HOSTNAME = "MixtureCloud.com";
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

            NULogger.getLogger().info("Trying to log in to mixturecloud.com");
            
            httpPost = new NUHttpPost("https://www.mixturecloud.com/login?lang=en");
            
            NULogger.getLogger().info("back: ");
            NULogger.getLogger().log(Level.INFO, "email: {0}", getUsername());
            NULogger.getLogger().info("login: 1");
            NULogger.getLogger().log(Level.INFO, "password: {0}", getPassword());
            NULogger.getLogger().log(Level.INFO, "recaptcha_challenge_field: {0}", recaptchaChallengeField);
            NULogger.getLogger().info("recaptcha_response_field: manual_challenge");
            NULogger.getLogger().log(Level.INFO, "securecode: {0}", secureCode);

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("back", ""));
            formparams.add(new BasicNameValuePair("email", getUsername()));
            formparams.add(new BasicNameValuePair("login", "1"));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            formparams.add(new BasicNameValuePair("recaptcha_challenge_field", recaptchaChallengeField));
            formparams.add(new BasicNameValuePair("recaptcha_response_field", "manual_challenge"));
            formparams.add(new BasicNameValuePair("securecode", secureCode));
            
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            Header lastHeader = httpResponse.getLastHeader("Location");
            if (lastHeader != null && lastHeader.getValue().contains("/")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                NULogger.getLogger().info("MixtureCloud login successful!");

            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                
                //FileUtils.saveInFile("MixtureCloudAccount.html", responseString);
                Document doc = Jsoup.parse(responseString);
                String error = doc.select("div#fullpage-container div#container.container div.msgerr").eq(1).text();
                
                if(error.contains("Your email or password are not valid")){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }

                //Generic exception
                throw new Exception("Login error: "+error);
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
        
        NULogger.getLogger().info("Getting startup secure code & captcha from mixturecloud.com");
        responseString = NUHttpClientUtils.getData("https://www.mixturecloud.com/login?lang=en", httpContext);
        Document doc = Jsoup.parse(responseString);
        secureCode = doc.select("div#fullpage-container div#container.container div#home.row div.span5 form#fsignin input[name=securecode]").first().val();
    
        //Get the captcha code
        Captcha captcha = captchaServiceProvider().newCaptcha();
        captcha.setFormTitle(Translation.T().captchacontrol()+" (MixtureCloud.com)");
        if(captcha.findCCaptchaUrlFromK(kChallengeURL+kChallengeCode) != null){
            captcha.findCaptchaImageURL();
            String captchaString = captcha.getCaptchaString();
            
            httpPost = new NUHttpPost("https://www.google.com/recaptcha/api/noscript?k="+kChallengeCode);
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("recaptcha_challenge_field", captcha.getCCaptchaUrl()));
            formparams.add(new BasicNameValuePair("recaptcha_response_field", captchaString));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            doc = Jsoup.parse(responseString);
            NULogger.getLogger().log(Level.INFO, "responseString: {0}", responseString);
            Element element = doc.select("textarea").first();
            if(element != null){
                recaptchaChallengeField = doc.select("textarea").first().text();
                NULogger.getLogger().log(Level.INFO, "recaptchaChallengeField: {0}", recaptchaChallengeField);
            }
            else{
                throw new NUCaptchaException(getHOSTNAME());
            }
        }
        else{
            throw new Exception("Captcha generic error");
        }
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
    
}
