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
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.uploaders.common.StringUtils;
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
public class SockShareAccount extends AbstractAccount{
    
    private HttpClient httpclient = NUHttpClient.getHttpClient(); //The httpclient for all the requests
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String captchaAddress;
    private Document doc;
    
    private String getUrlLogin = "http://www.sockshare.com/authenticate.php?login";
    private String postUrlLogin = "http://www.sockshare.com/authenticate.php?login";
    
    
    public SockShareAccount(){
        KEY_USERNAME = "scksusername";
        KEY_PASSWORD = "sckspassword";
        HOSTNAME = "SockShare.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
    }

    @Override
    public void login() {
        Captcha captcha = captchaServiceProvider().newCaptcha();
        String captchaString;
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        UrlEncodedFormEntity entity;
        httpPost = new NUHttpPost(postUrlLogin);
        
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            responseString = NUHttpClientUtils.getData(getUrlLogin, httpContext);
            
            //Find the image of the captcha
            captchaAddress = StringUtils.stringBetweenTwoStrings(responseString, "<img src=\"/include/captcha.php", "\"");
            captchaAddress = "http://www.sockshare.com/include/captcha.php"+captchaAddress;
            captchaAddress = captchaAddress.replace("&amp;", "&");
            NULogger.getLogger().log(Level.INFO, "captchaAddress: {0}", captchaAddress);
            
            captcha.setFormTitle(Translation.T().captchacontrol()+" (SockShare.com)");
            captcha.setImageURL(captchaAddress);
            captcha.setHttpContext(httpContext);
            captchaString = captcha.getCaptchaString(); //Read captcha string
            
            //Now we can send the request
            formparams.add(new BasicNameValuePair("user", getUsername()));
            formparams.add(new BasicNameValuePair("pass", getPassword()));
            formparams.add(new BasicNameValuePair("login_submit", "Login"));
            formparams.add(new BasicNameValuePair("captcha_code", captchaString));
            entity = new UrlEncodedFormEntity(formparams);

            httpPost.setEntity(entity);
            
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            if(CookieUtils.existCookie(httpContext, "auth")){
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("SockShare login successful");
            }
            else{
                //Handle errors
                String error;
                responseString = NUHttpClientUtils.getData(getUrlLogin, httpContext);
                
                doc = Jsoup.parse(responseString);

                error = doc.select("div.container-back div.container div.site-body div.site-content div.message").first().text();
                
                if("Please re-enter the captcha code".equals(error)){
                    throw new NUCaptchaException(getHOSTNAME());
                }
                
                if("No such username or wrong password".equals(error)){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
                
                //FileUtils.saveInFile("SockShareAccount.html", responseString);
                
                throw new Exception("Login doesn't work: captcha or username/password incorrect!");
            }
            
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
            
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.SEVERE, "SockShare Login warning {0}", ex);
            resetLogin();
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }
    
    /**
     * Reset login.
     */
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
    
}
