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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Dinesh
 */
public class SendSpaceAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;

    private static String sidcookie = "";
    private static String ssuicookie = "", ssalcookie = "";

    public SendSpaceAccount() {
        KEY_USERNAME = "ssusername";
        KEY_PASSWORD = "sspassword";
        HOSTNAME = "SendSpace.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    private void initialize() throws Exception {
        NULogger.getLogger().info("Getting startup cookie from sendspace.com");
        httpGet = new NUHttpGet("http://www.sendspace.com/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        sidcookie = CookieUtils.getCookieNameValue(httpContext, "SID");
        ssuicookie = CookieUtils.getCookieNameValue(httpContext, "ssui");

        NULogger.getLogger().log(Level.INFO, "sidcookie: {0}", sidcookie);
        NULogger.getLogger().log(Level.INFO, "ssuicookie: {0}", ssuicookie);

    }

    public void loginSendSpace() throws Exception {
        loginsuccessful = false;

        NULogger.getLogger().info("Trying to log in to sendspace");
        httpPost = new NUHttpPost("http://www.sendspace.com/login.html");
        
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("action", "login"));
        formparams.add(new BasicNameValuePair("submit", "login"));
        formparams.add(new BasicNameValuePair("target", "%252F"));
        formparams.add(new BasicNameValuePair("action_type", "login"));
        formparams.add(new BasicNameValuePair("remember", "1"));
        formparams.add(new BasicNameValuePair("username", getUsername()));
        formparams.add(new BasicNameValuePair("password", getPassword()));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());

        NULogger.getLogger().info("Getting cookies........");

        if (CookieUtils.existCookie(httpContext, "ssal")) {
            ssalcookie = CookieUtils.getCookieNameValue(httpContext, "ssal");
            NULogger.getLogger().info(ssalcookie);
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            NULogger.getLogger().info("SendSpace login success :)");
        } else {
            //Handle errors
            String error;
            Document doc;
            
            doc = Jsoup.parse(stringResponse);
            error = doc.select("div#content div.msg").text();
            
            if(error.contains("Login failed. Please check your username and password.")){
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }
            
            //Other errors here
            
            //FileUtils.saveInFile("SendSpaceAccount.html", stringResponse);
            
            //Generic error
            throw new Exception("Exception in SendSpace login: "+error);
            
        }

    }

    public static String getSidcookie() {
        return sidcookie;
    }

    public static String getSsalcookie() {
        return ssalcookie;
    }

    public static String getSsuicookie() {
        return ssuicookie;
    }

    @Override
    public void login() {
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            initialize();
            loginSendSpace();
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            NULogger.getLogger().log(Level.SEVERE, "Error in SendSpace Login {0}", e);
            resetLogin();
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }

    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
        sidcookie = "";
        ssalcookie = "";
        ssuicookie = "";
    }
}
