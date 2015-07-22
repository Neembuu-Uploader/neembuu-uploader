/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
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
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.CookieUtils;
//import neembuuuploader.captcha.Captcha;
import neembuu.uploader.utils.NUHttpClientUtils;

/**
 *
 * @author Paralytic
 */
public class NitroFlareAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
	
    //private String captcha_url = "";
    //private String captchaString = "";
    private String nf_token = "";

    public NitroFlareAccount() {
        KEY_USERNAME = "ntrflareusername";
        KEY_PASSWORD = "ntrflarepassword";
        HOSTNAME = "NitroFlare.com";
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

            NULogger.getLogger().info("Trying to log in to NitroFlare.com");
            httpPost = new NUHttpPost("https://nitroflare.com/login");
            httpPost.setHeader("Referer", "https://nitroflare.com/login");
            httpPost.setHeader("Host", "nitroflare.com");
            httpPost.setHeader("DNT", "1");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:36.0) Gecko/20100101 Firefox/36.0");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            //formparams.add(new BasicNameValuePair("captcha", captchaString));
            formparams.add(new BasicNameValuePair("email", getUsername()));
            formparams.add(new BasicNameValuePair("login", ""));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            formparams.add(new BasicNameValuePair("token", nf_token));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            
            if (!CookieUtils.getCookieValue(httpContext, "user").isEmpty()) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                NULogger.getLogger().info("NitroFlare.com login successful!");

            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("NitroFlareAccount.html", responseString);
                Document doc = Jsoup.parse(responseString);
                String error = doc.select(".errors li").first().text();
                
                if(error.contains("Account does not exist")){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }
                else if(error.contains("Login failed")){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }
                else if(error.contains("CAPTCHA error")){
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
        NULogger.getLogger().info("Getting startup cookies & link from NitroFlare.com");
        responseString = NUHttpClientUtils.getData("https://nitroflare.com/login", httpContext);
        
	nf_token = StringUtils.stringBetweenTwoStrings(responseString, "name=\"token\" value=\"", "\"");
	/*captcha_url = "https://www.nitroflare.com/plugins/captcha/CaptchaSecurityImages.php" + StringUtils.stringBetweenTwoStrings(responseString, "plugins/captcha/CaptchaSecurityImages.php", "\"");
        Captcha captcha = captchaServiceProvider().newCaptcha();
	captcha.setFormTitle(Translation.T().captchacontrol()+" (NitroFlare.com)");
        captcha.setImageURL(captcha_url);
        captcha.setHttpContext(httpContext);
        captchaString = captcha.getCaptchaString();*/
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
