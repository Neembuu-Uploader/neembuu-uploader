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
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Dinesh
 * @author davidepastore
 */
public class UploadedDotToAccount extends AbstractAccount {

    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    private String phpsessioncookie = "";
    private String logincookie = "";
    private String authcookie = "";

    public UploadedDotToAccount() {
        KEY_USERNAME = "udtusername";
        KEY_PASSWORD = "udtpassword";
        HOSTNAME = "Uploaded.net";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    public String getLogincookie() {
        return logincookie;
    }

    public String getPhpsessioncookie() {
        return phpsessioncookie;
    }

    public String getAuthcookie() {
        return authcookie;
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();

            NULogger.getLogger().info("Trying to log in to uploaded.net");
            httpPost = new NUHttpPost("http://uploaded.net/io/login");
            
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("id", getUsername()));
            formparams.add(new BasicNameValuePair("pw", getPassword()));
            //formparams.add(new BasicNameValuePair("_", ""));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept","text/javascript, text/html, application/xml, text/xml, */*");
            httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
            httpPost.setHeader("X-Prototype-Version", "1.6.1");
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            NULogger.getLogger().info("Getting cookies........");
            
            if(CookieUtils.existCookie(httpContext, "login")){
                logincookie = CookieUtils.getCookieValue(httpContext, "login");
                authcookie = CookieUtils.getCookieValue(httpContext, "auth");
                loginsuccessful = true;
            }
            
            if (loginsuccessful) {
                username = getUsername();
                password = getPassword();
                
                //Set user type
                responseString = NUHttpClientUtils.getData("http://uploaded.net/me", httpContext);
                Document doc = Jsoup.parse(responseString);
                if(!"Free".equals(doc.select("div#account div.box table.data tbody tr th a em").text())){
                    premium = true;
                    NULogger.getLogger().info("Premium user");
                }
                else{
                    NULogger.getLogger().log(Level.INFO, "Other user type: {0}", doc.select("div#account div.box table.data tbody tr th a em").text());
                }
                
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                NULogger.getLogger().info("Uploaded.net Login success :)");
            } else {
                //Get error message
                JSONObject jSonObject = new JSONObject(responseString);
                String error = jSonObject.getString("err");
                
                if(error.equals("User and password do not match!")){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }
                
                //Generic error
                throw new Exception("Login error: " + error);
            }

        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "Uploaded.net login exception: {0}", e);
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }
    
    private void initialize() throws Exception{
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        
        NULogger.getLogger().info("Getting startup cookie from uploaded.net");
        responseString = NUHttpClientUtils.getData("http://uploaded.net/", httpContext);
        phpsessioncookie = CookieUtils.getCookieValue(httpContext, "PHPSESSID");

        NULogger.getLogger().log(Level.INFO, "phpsessioncookie: {0}", phpsessioncookie);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
