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
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.uploaders.common.StringUtils;
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
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NUHttpClientUtils;

/**
 *
 * @author Paralytic
 */
public class CloudCornerAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    public String session_id = "";
    public String master_key = "";
    
    //Captcha
    private boolean isCaptReq = false;
    private final String kChallengeURL = "https://www.google.com/recaptcha/api/challenge?k=";
    private String kChallengeCode = "";
    private String recaptchaChallengeField;
    private String captchaString = "";

    public CloudCornerAccount() {
        KEY_USERNAME = "cldcrnrusername";
        KEY_PASSWORD = "cldcrnrpassword";
        HOSTNAME = "CloudCorner.com";
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

            NULogger.getLogger().info("** CloudCorner.com ** => Attempting to login ...");
            httpPost = new NUHttpPost("https://www.cloudcorner.com/api/login/");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("remember", "true"));
            formparams.add(new BasicNameValuePair("email", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            if (isCaptReq){
                formparams.add(new BasicNameValuePair("recaptcha_challenge", recaptchaChallengeField));
                formparams.add(new BasicNameValuePair("recaptcha_response", captchaString));
            }
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            responseString = StringUtils.stringBetweenTwoStrings(responseString, "\"success\":", ",\"");
            session_id = CookieUtils.getCookieValue(httpContext, "session_id");
            master_key = CookieUtils.getCookieValue(httpContext, "master_key");

            if (responseString.equals("true") && !session_id.isEmpty() && !master_key.isEmpty()) {
                NULogger.getLogger().info("** CloudCorner.com ** => (1/3): Received session ID.");
                NULogger.getLogger().info("** CloudCorner.com ** => (2/3): Received master key.");
                NULogger.getLogger().info("** CloudCorner.com ** => (3/3): Logged in successfully!");
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                Document doc = Jsoup.parse(responseString);
                String error = doc.select(".err").first().text();
                
                if("Incorrect Login or Password".equals(error)){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }

                //Generic exception
                throw new Exception("Login error: " + error);
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

        NULogger.getLogger().info("** CloudCorner.com ** => Gathering initial cookie values.");
        responseString = NUHttpClientUtils.getData("https://www.cloudcorner.com/", httpContext);
        responseString = NUHttpClientUtils.getData("https://www.cloudcorner.com/api/recaptcha", httpContext);
        // {"success":true,"data":{"enable":true,"key":"6Ld7c9ISAAAAADeAGevtJ0zpz4nfVmjUNt5-Caqh"}}
        
        if (StringUtils.stringBetweenTwoStrings(responseString, "\"enable\":", ",\"").equals("true")){
            isCaptReq = true;
            NULogger.getLogger().info("** CloudCorner.com ** => Captcha is being enforced ...");
            kChallengeCode = StringUtils.stringBetweenTwoStrings(responseString, "\"key\":\"", "\"");
            
            Captcha captcha = captchaServiceProvider().newCaptcha();
            captcha.setFormTitle(Translation.T().captchacontrol()+" ("+ getHOSTNAME()  +")");
            if(captcha.findCCaptchaUrlFromK(kChallengeURL+kChallengeCode) != null){
                captcha.findCaptchaImageURL();
                recaptchaChallengeField = captcha.getCCaptchaUrl();
                captchaString = captcha.getCaptchaString();
                NULogger.getLogger().info("** CloudCorner.com ** => Captcha string input by the user: "+captchaString);
            } else {
                throw new Exception("Captcha generic error");
            }
        }
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}