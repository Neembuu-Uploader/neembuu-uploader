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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Paralytic
 */
public class PromptFileAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    private Pattern p;
    private Matcher m;
    private String captcha_sid = "";
    private String captchaString = "";
    private String captcha_url = "";

    public PromptFileAccount() {
        KEY_USERNAME = "prmptflusername";
        KEY_PASSWORD = "prmptflpassword";
        HOSTNAME = "PromptFile.com";
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
            
            NULogger.getLogger().info("Trying to log in to PromptFile.com");
            httpPost = new NUHttpPost("http://www.promptfile.com/actions.php");
            
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("action", "login"));
            formparams.add(new BasicNameValuePair("data[code]", captchaString));
            formparams.add(new BasicNameValuePair("data[login]", getUsername()));
            formparams.add(new BasicNameValuePair("data[password]", getPassword()));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            if (httpResponse != null && httpResponse.toString().contains("pfauth=")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("PromptFile.com login successful!");
            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                Document doc = Jsoup.parse(responseString);
                String error = doc.select("Wrong login or password").first().text();
                
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

        httpPost = new NUHttpPost("http://www.promptfile.com/modal.php");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("modal", "login"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        responseString = EntityUtils.toString(httpResponse.getEntity());
        
        p = Pattern.compile("sid=[a-z0-9]*(?=\")");
        m = p.matcher(responseString);

        while (m.find()){
            captcha_sid = m.group();
        }
        
        captcha_url = "http://www.promptfile.com/securimage_show.php?" + captcha_sid;
        
        Captcha captcha = captchaServiceProvider().newCaptcha();
        captcha.setFormTitle(Translation.T().captchacontrol()+" (PromptFile.com)");
        captcha.setImageURL(captcha_url);
        captcha.setHttpContext(httpContext);
        captchaString = captcha.getCaptchaString();
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
