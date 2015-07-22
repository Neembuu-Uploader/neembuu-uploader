/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.io.IOException;
import java.net.MalformedURLException;
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
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * If there is a problem, take a look to <a href="http://dfiles.eu/filemanager.html">Depositfiles Filemanager</a>.
 * @author dinesh
 * @author davidepastore
 */
public class DepositFilesAccount extends AbstractAccount {

    private static String uprandcookie = "";
    private static String autologincookie = "";
    private static String alcookie = "";
    
    /* For login */
    private String keyName = "key5043";
    private String keyValue = "val1358082508203";
    
    /* For captcha */
    private String kChallengeURL = "http://www.google.com/recaptcha/api/challenge?k=";
    private String kChallengeCode = "6LdRTL8SAAAAAE9UOdWZ4d0Ky-aeA7XfSqyWDM2m";
    private String downloadManagerVersion;
    private String captchaString;
    //private final int numTests = 3; //The number of tests for login
    private HttpClient httpclient = NUHttpClient.getHttpClient(); //The httpclient for all the requests
    private HttpResponse httpResponse;
    private NUHttpGet httpGet;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private JSONObject jsonObj;

    public DepositFilesAccount() {
        KEY_USERNAME = "dfusername";
        KEY_PASSWORD = "dfpassword";
        HOSTNAME = "DepositFiles.com";
    }

    @Override
    public void login() {
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            
            //setDownloadManagerVersion();
            setUprandCookie();
            NULogger.getLogger().info("Login to deposifiles...");
            loginWithCaptcha();
            NULogger.getLogger().info("DepositFiles login successful");

        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
            
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.SEVERE, "DepositFiles Login warning {0}", ex.toString());
            resetLogin();
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }

    /**
     * Set uprand cookie.
     * @throws IOException 
     */
    public void setUprandCookie() throws IOException {
        httpGet = new NUHttpGet("http://www.dfiles.eu/");
        HttpParams params = new BasicHttpParams();
        params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        httpGet.setParams(params);
        httpResponse = httpclient.execute(httpGet, httpContext);
        EntityUtils.toString(httpResponse.getEntity()); //Consume content
        
        uprandcookie = CookieUtils.getCookieValue(httpContext, "uprand");
        NULogger.getLogger().log(Level.INFO, "Uprand cookie from depositfiles.com : {0}", uprandcookie);
    }

    public static String getUprandcookie() {
        return uprandcookie;
    }

    public static String getAutologincookie() {
        return autologincookie;
    }
    
    public static String getAlcookie() {
        return alcookie;
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }
    
    public void resetLogin(){
        loginsuccessful = false;
        uprandcookie = "";
        autologincookie = "";
        alcookie = "";
        username = "";
        password = "";
    }

    /**
     * Save login cookie for next login.
     * @param response HttpResponse from the request (with captcha).
     * @return The status of the login. True if it works, false otherwise.
     * @throws IOException
     * @throws JSONException 
     */
    private boolean saveLoginCookie(HttpResponse response) throws IOException, JSONException, Exception{
        String error, status, token;
        boolean cookieFound = false;
        
        if(CookieUtils.existCookieStartWithValue(httpContext, "al_")){
            alcookie = CookieUtils.getCookieStartsWithValue(httpContext, "al_");
            cookieFound = true;
        }

        NULogger.getLogger().log(Level.INFO, "al cookie: {0}", alcookie);

        String reqResponse = EntityUtils.toString(response.getEntity());
        
        //CommonUploaderTasks.saveInFile("depositfilesAfterLogin.json", reqResponse);
        
        //Status
        jsonObj = new JSONObject(reqResponse);
        status = jsonObj.getString("status");
        //NULogger.getLogger().log(Level.INFO, "JSONObject: {0}", jsonObj);
        //NULogger.getLogger().log(Level.INFO, "Your status is: {0}", status);
        
        //Error handlers. Add here other error conditions.
        if("Error".equals(status)){
            error = jsonObj.getString("error");
            
            //If you set an invalid login
            if("LoginInvalid".equals(error)){
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }
            
            //If captcha is required
            if("CaptchaRequired".equals(error)){
                return false;
            }
            
            //If captcha is invalid
            if("CaptchaInvalid".equals(error)){
                throw new NUCaptchaException(getHOSTNAME());
            }
            
            throw new Exception("Error: " + error);
        }

        //Autologin cookie
        JSONObject jsonSon = jsonObj.getJSONObject("data");
        token = jsonSon.getString("token");
        autologincookie = token;

        //If al cookie is not found, repeat all
        if(!cookieFound){
            return false;
        }

        return status != null;
    }

    /**
     * Set the download manager version of depositfiles.com.
     * 
     */
    private void setDownloadManagerVersion() throws Exception {
        downloadManagerVersion = NUHttpClientUtils.getData("http://system.dfiles.eu/api/get_downloader_version.php", httpContext);
    }

    /**
     * Login with captcha.
     */
    private void loginWithCaptcha() throws MalformedURLException, IOException, Exception{
        //String getURL = "http://www.dfiles.eu/nl/login.php?return%2Fgold%2F";
        String postURL = "http://dfiles.eu/api/user/login";
        boolean status;
        List<NameValuePair> formparams;
        HttpParams params;
        
        //Testing normal login with key (depositfiles is strange)
        httpPost = new NUHttpPost(postURL);
        params = new BasicHttpParams();
        params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        httpPost.setParams(params);
        
        formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("login", getUsername()));
        formparams.add(new BasicNameValuePair("password", getPassword()));
        formparams.add(new BasicNameValuePair(keyName, keyValue));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams);
        
        httpPost.setEntity(entity);

        httpResponse = httpclient.execute(httpPost, httpContext);

        status = saveLoginCookie(httpResponse);
        
        if(!status){

            //for(int i = 0; i < this.numTests && status == false; i++){
                //CommonUploaderTasks.saveInFile("DepositfilesResponse.html", reqResponse);

                Captcha captcha = captchaServiceProvider().newCaptcha();
                captcha.setFormTitle(Translation.T().captchacontrol()+" (DepositFiles.com)");
                if(captcha.findCCaptchaUrlFromK(kChallengeURL+kChallengeCode) != null){
                    captcha.findCaptchaImageURL();
                    captchaString = captcha.getCaptchaString();

                    //Send captcha
                    httpPost = new NUHttpPost(postURL);
                    params = new BasicHttpParams();
                    params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
                    httpPost.setParams(params);

                    formparams = new ArrayList<NameValuePair>();
                    formparams.add(new BasicNameValuePair("login", getUsername()));
                    formparams.add(new BasicNameValuePair("password", getPassword()));
                    formparams.add(new BasicNameValuePair("recaptcha_challenge_field", captcha.getCCaptchaUrl()));
                    formparams.add(new BasicNameValuePair("recaptcha_response_field", captchaString));
                    entity = new UrlEncodedFormEntity(formparams);

                    httpPost.setEntity(entity);

                    httpResponse = httpclient.execute(httpPost, httpContext);

                    status = saveLoginCookie(httpResponse);

                    NULogger.getLogger().log(Level.INFO, "Your status is: {0}", status);
                }
            //}
        }
        
        if(status){
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            NULogger.getLogger().info("DepositFiles login successful :)");
        }
        else{
            throw new Exception("Login doesn't work: take a look to .json file or captcha.");
        }
    }
}
