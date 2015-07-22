/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

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
import org.apache.http.HttpEntity;
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
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Dinesh
 * @author davidepastore
 */
public class ImageShackAccount extends AbstractAccount {

    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpGet httpGet;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private JSONObject jsonObject;
    
    private String langcookie;
    private String latestcookie;
    private String uncookie;
    private String imgshckcookie;
    private String phpsessioncookie;
    private String newcookie;
    private String myidcookie;
    private static String myimagescookie;
    private static String usercookie;
    private static String upload_key;

    public ImageShackAccount() {
        KEY_USERNAME = "isusername";
        KEY_PASSWORD = "ispassword";
        HOSTNAME = "ImageShack.us";
    }

    public String getUsercookie() {
        return usercookie;
    }

    public String getMyimagescookie() {
        return myimagescookie;
    }

    public String getUpload_key() {
        return upload_key;
    }

    @Override
    public void disableLogin() {
        resetLogin();
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    private void initialize() throws Exception {
        NULogger.getLogger().info("Getting startup cookie from imageshack.us");
        httpGet = new NUHttpGet("http://imageshack.us/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        langcookie = CookieUtils.getCookieNameValue(httpContext, "lang");
        latestcookie = CookieUtils.getCookieNameValue(httpContext, "latest");
        uncookie = CookieUtils.getCookieNameValue(httpContext, "un_");
        imgshckcookie = CookieUtils.getCookieNameValue(httpContext, "imgshck");
        phpsessioncookie = CookieUtils.getCookieNameValue(httpContext, "PHPSESSID");
        newcookie = CookieUtils.getCookieNameValue(httpContext, "new_");
        
        //FileUtils.saveInFile("ImageShackAccount.html", stringResponse);
        Document doc = Jsoup.parse(stringResponse);
        upload_key = doc.select("div.layout div.indent div#column-left.column-left div#upload_canvas form#upform div#uploadform input[name=key]").val();
        NULogger.getLogger().log(Level.INFO, "upload_key : {0}", upload_key);
        NULogger.getLogger().log(Level.INFO, "langcookie : {0}", langcookie);
        NULogger.getLogger().log(Level.INFO, "latestcookie : {0}", latestcookie);
        NULogger.getLogger().log(Level.INFO, "uncookie : {0}", uncookie);
        NULogger.getLogger().log(Level.INFO, "imgshckcookie : {0}", imgshckcookie);
        NULogger.getLogger().log(Level.INFO, "phpsessioncookie : {0}", phpsessioncookie);
        NULogger.getLogger().log(Level.INFO, "newcookie : {0}", newcookie);
    }

    private void loginImageShack() throws Exception {

        loginsuccessful = false;
        /*
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);
        */
        NULogger.getLogger().info("Trying to log in to imageshack.us");
        httpPost = new NUHttpPost("http://imageshack.us/auth.php");
        //httppost.setHeader("Referer", "http://www.uploading.com/");
        //httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        //httppost.setHeader("Cookie", newcookie + ";" + phpsessioncookie + ";" + imgshckcookie + ";" + uncookie + ";" + latestcookie + ";" + langcookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("username", getUsername()));
        formparams.add(new BasicNameValuePair("password", getPassword()));
        formparams.add(new BasicNameValuePair("stay_logged_in", ""));
        formparams.add(new BasicNameValuePair("format", "json"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity en = httpResponse.getEntity();
        stringResponse = EntityUtils.toString(en);
        
        NULogger.getLogger().info("Getting cookies........");
        if(CookieUtils.existCookie(httpContext, "myid")){
            loginsuccessful = true;
            myidcookie = CookieUtils.getCookieValue(httpContext, "myid");
            myimagescookie = CookieUtils.getCookieValue(httpContext, "myimages");
            usercookie = CookieUtils.getCookieValue(httpContext, "isUSER");
        }
        /*
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            if (escookie.getName().equalsIgnoreCase("myid")) {
                myidcookie = escookie.getValue();
                NULogger.getLogger().info(myidcookie);
                loginsuccessful = true;

            }
            if (escookie.getName().equalsIgnoreCase("myimages")) {
                myimagescookie = escookie.getValue();
                NULogger.getLogger().info(myimagescookie);
            }
            if (escookie.getName().equalsIgnoreCase("isUSER")) {
                usercookie = escookie.getValue();
                NULogger.getLogger().info(usercookie);
            }
        }
        */
        if (loginsuccessful) {
            NULogger.getLogger().info("ImageShack Login Success");
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
        } else {
            //Handle errors
            //FileUtils.saveInFile("TwoSharedAccount.html", stringResponse);
            //NULogger.getLogger().log(Level.INFO, "Login response : {0}", stringResponse);
            jsonObject = new JSONObject(stringResponse);
            if(!jsonObject.getBoolean("status")){
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }
            //Generic error
            throw new Exception("Error in ImageShack Login: "+stringResponse);
        }

    }

    @Override
    public void login() {

        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            initialize();
            loginImageShack();
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception ex) {
            resetLogin();
            Logger.getLogger(FileSonicAccount.class.getName()).log(Level.SEVERE, null, ex);
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }
    
    
    private void resetLogin(){
        loginsuccessful = false;
        langcookie = "";
        latestcookie = "";
        uncookie = "";
        imgshckcookie = "";
        phpsessioncookie = "";
        newcookie = "";
        myidcookie = "";
        myimagescookie = "";
        usercookie = "";
        username = "";
        password = "";
    }
}
