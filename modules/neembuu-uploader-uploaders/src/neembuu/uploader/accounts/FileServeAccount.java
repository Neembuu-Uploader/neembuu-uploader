/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.jsoup.nodes.Element;

/**
 *
 * @author dinesh
 */
public class FileServeAccount extends AbstractAccount {

    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;
    private String href;
    private Document doc;
    
    private static String sessioncookie = "";
    private static String dashboardcookie = "";

    public FileServeAccount() {
        KEY_USERNAME = "fsrvusername";
        KEY_PASSWORD = "fsrvpassword";
        HOSTNAME = "FileServe.com";
    }

    @Override
    public void login() {
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            initialize();
            NULogger.getLogger().info("Login to FileServe.com");
            
            httpPost = new NUHttpPost("http://fileserve.com/login.php");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("loginUserName", getUsername()));
            formparams.add(new BasicNameValuePair("loginUserPassword", getPassword()));
            formparams.add(new BasicNameValuePair("ppp", "102"));
            formparams.add(new BasicNameValuePair("loginFormSubmit", "Login"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(stringResponse);
            Element aLink = doc.select("div#container div.middle div.yellow_tips a.text_link").first();
            if(aLink == null){
                //The element does not exist
                //FileUtils.saveInFile("FileServeAccount.html", stringResponse);
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }
            
            href = aLink.attr("href");
            
            if("/dashboard.php".equals(href)){
                httpGet = new NUHttpGet("http://fileserve.com/dashboard.php");
                httpResponse = httpclient.execute(httpGet, httpContext);
                stringResponse = EntityUtils.toString(httpResponse.getEntity());
                if(CookieUtils.existCookie(httpContext, "cookie")){
                    //Login success
                    dashboardcookie = CookieUtils.getCookieNameValue(httpContext, "cookie");
                    NULogger.getLogger().log(Level.INFO, "dashboard cookie : {0}", dashboardcookie);
                    loginsuccessful = true;
                    hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                    username = getUsername();
                    password = getPassword();
                    NULogger.getLogger().info("FileServe Login successful!");
                }
                else{
                    //Login failed
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
            }
            
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception ex) {
            resetLogin();
            NULogger.getLogger().info("FileServe Login failed");
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
            Logger.getLogger(FileServeAccount.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void disableLogin() {
        resetLogin();
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    private void initialize() throws IOException {
        NULogger.getLogger().info("Getting start up cookie from FileServe.com");
        httpGet = new NUHttpGet("http://www.fileserve.com/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        sessioncookie = CookieUtils.getCookieNameValue(httpContext, "PHPSESSID");
        NULogger.getLogger().log(Level.INFO, "fileserve session cookie : {0}", sessioncookie);
    }

    public String getDashboardcookie() {
        return dashboardcookie;
    }
    
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
